#!/bin/bash

###############################################################################
# AWS Deployment Setup Script
# This script automates the creation of AWS resources needed for CD pipeline
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
BUCKET_NAME="tictactoe-deployment-${USER}-$(date +%s)"
IAM_USER_NAME="tictactoe-github-actions"
IAM_ROLE_NAME="tictactoe-ec2-deployment-role"
INSTANCE_PROFILE_NAME="tictactoe-ec2-deployment-profile"
REGION=""
INSTANCE_ID=""

print_header() {
    echo -e "\n${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Function to check if AWS CLI is installed
check_aws_cli() {
    if ! command -v aws &> /dev/null; then
        print_error "AWS CLI is not installed. Please install it first."
        echo "Visit: https://aws.amazon.com/cli/"
        exit 1
    fi
    print_success "AWS CLI is installed"
}

# Function to check AWS credentials
check_aws_credentials() {
    if ! aws sts get-caller-identity &> /dev/null; then
        print_error "AWS credentials are not configured or invalid."
        echo "Run: aws configure"
        exit 1
    fi
    
    ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
    CURRENT_USER=$(aws sts get-caller-identity --query Arn --output text)
    print_success "AWS credentials are valid"
    print_info "Account ID: $ACCOUNT_ID"
    print_info "Current user: $CURRENT_USER"
}

# Function to get AWS region
get_region() {
    if [ -z "$REGION" ]; then
        REGION=$(aws configure get region)
        if [ -z "$REGION" ]; then
            print_warning "No default region configured."
            read -p "Enter AWS region (e.g., us-east-1): " REGION
        fi
    fi
    print_info "Using region: $REGION"
}

# Function to get EC2 instance ID
get_instance_id() {
    if [ -z "$INSTANCE_ID" ]; then
        echo ""
        print_info "Fetching EC2 instances..."
        aws ec2 describe-instances \
            --region "$REGION" \
            --query 'Reservations[*].Instances[?State.Name==`running`].[InstanceId,Tags[?Key==`Name`].Value|[0],PublicIpAddress]' \
            --output table
        
        echo ""
        read -p "Enter your EC2 Instance ID: " INSTANCE_ID
    fi
    print_info "Using instance: $INSTANCE_ID"
}

# Function to create S3 bucket
create_s3_bucket() {
    print_header "Creating S3 Bucket"
    
    if aws s3 ls "s3://$BUCKET_NAME" 2>/dev/null; then
        print_warning "Bucket $BUCKET_NAME already exists"
    else
        if [ "$REGION" = "us-east-1" ]; then
            aws s3api create-bucket \
                --bucket "$BUCKET_NAME" \
                --region "$REGION"
        else
            aws s3api create-bucket \
                --bucket "$BUCKET_NAME" \
                --region "$REGION" \
                --create-bucket-configuration LocationConstraint="$REGION"
        fi
        print_success "Created S3 bucket: $BUCKET_NAME"
    fi
    
    # Enable versioning
    aws s3api put-bucket-versioning \
        --bucket "$BUCKET_NAME" \
        --versioning-configuration Status=Enabled \
        --region "$REGION"
    print_success "Enabled versioning on bucket"
    
    # Block public access
    aws s3api put-public-access-block \
        --bucket "$BUCKET_NAME" \
        --public-access-block-configuration \
        "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true" \
        --region "$REGION"
    print_success "Configured public access block"
    
    # Add lifecycle policy to clean old deployments
    cat > /tmp/lifecycle-policy.json << EOF
{
    "Rules": [
        {
            "ID": "DeleteOldDeployments",
            "Status": "Enabled",
            "Prefix": "deployments/",
            "Expiration": {
                "Days": 7
            },
            "NoncurrentVersionExpiration": {
                "NoncurrentDays": 3
            }
        }
    ]
}
EOF
    
    aws s3api put-bucket-lifecycle-configuration \
        --bucket "$BUCKET_NAME" \
        --lifecycle-configuration file:///tmp/lifecycle-policy.json \
        --region "$REGION"
    print_success "Configured lifecycle policy (7 days retention)"
    
    rm /tmp/lifecycle-policy.json
}

# Function to create IAM role for EC2
create_ec2_iam_role() {
    print_header "Creating IAM Role for EC2"
    
    # Create trust policy
    cat > /tmp/trust-policy.json << EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Principal": {
                "Service": "ec2.amazonaws.com"
            },
            "Action": "sts:AssumeRole"
        }
    ]
}
EOF
    
    # Create role
    if aws iam get-role --role-name "$IAM_ROLE_NAME" 2>/dev/null; then
        print_warning "IAM role $IAM_ROLE_NAME already exists"
    else
        aws iam create-role \
            --role-name "$IAM_ROLE_NAME" \
            --assume-role-policy-document file:///tmp/trust-policy.json \
            --description "Role for EC2 instance to access S3 for deployments"
        print_success "Created IAM role: $IAM_ROLE_NAME"
    fi
    
    rm /tmp/trust-policy.json
    
    # Create inline policy for S3 access
    cat > /tmp/s3-policy.json << EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "S3DeploymentAccess",
            "Effect": "Allow",
            "Action": [
                "s3:GetObject",
                "s3:ListBucket"
            ],
            "Resource": [
                "arn:aws:s3:::${BUCKET_NAME}",
                "arn:aws:s3:::${BUCKET_NAME}/*"
            ]
        }
    ]
}
EOF
    
    aws iam put-role-policy \
        --role-name "$IAM_ROLE_NAME" \
        --policy-name "S3DeploymentAccess" \
        --policy-document file:///tmp/s3-policy.json
    print_success "Attached S3 access policy to role"
    
    rm /tmp/s3-policy.json
    
    # Attach AWS managed policy for SSM
    aws iam attach-role-policy \
        --role-name "$IAM_ROLE_NAME" \
        --policy-arn "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore" 2>/dev/null || true
    print_success "Attached SSM managed policy to role"
    
    # Create instance profile
    if aws iam get-instance-profile --instance-profile-name "$INSTANCE_PROFILE_NAME" 2>/dev/null; then
        print_warning "Instance profile $INSTANCE_PROFILE_NAME already exists"
    else
        aws iam create-instance-profile \
            --instance-profile-name "$INSTANCE_PROFILE_NAME"
        print_success "Created instance profile: $INSTANCE_PROFILE_NAME"
        
        # Wait a bit for the profile to be ready
        sleep 2
    fi
    
    # Add role to instance profile
    aws iam add-role-to-instance-profile \
        --instance-profile-name "$INSTANCE_PROFILE_NAME" \
        --role-name "$IAM_ROLE_NAME" 2>/dev/null || print_warning "Role already in instance profile"
    
    print_success "Instance profile configured"
}

# Function to attach IAM role to EC2 instance
attach_role_to_instance() {
    print_header "Attaching IAM Role to EC2 Instance"
    
    # Check if instance already has a role
    CURRENT_PROFILE=$(aws ec2 describe-instances \
        --instance-ids "$INSTANCE_ID" \
        --region "$REGION" \
        --query 'Reservations[0].Instances[0].IamInstanceProfile.Arn' \
        --output text 2>/dev/null || echo "None")
    
    if [ "$CURRENT_PROFILE" != "None" ]; then
        print_warning "Instance already has an IAM instance profile attached"
        read -p "Do you want to replace it with the new one? (y/n): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_info "Skipping instance profile attachment"
            return
        fi
        
        # Disassociate current profile
        ASSOCIATION_ID=$(aws ec2 describe-iam-instance-profile-associations \
            --filters "Name=instance-id,Values=$INSTANCE_ID" \
            --region "$REGION" \
            --query 'IamInstanceProfileAssociations[0].AssociationId' \
            --output text)
        
        if [ "$ASSOCIATION_ID" != "None" ]; then
            aws ec2 disassociate-iam-instance-profile \
                --association-id "$ASSOCIATION_ID" \
                --region "$REGION"
            print_info "Disassociated old instance profile"
            sleep 5
        fi
    fi
    
    # Attach new instance profile
    aws ec2 associate-iam-instance-profile \
        --instance-id "$INSTANCE_ID" \
        --iam-instance-profile "Name=$INSTANCE_PROFILE_NAME" \
        --region "$REGION"
    print_success "Attached instance profile to EC2 instance"
    print_warning "You may need to restart the instance for SSM agent to pick up new permissions"
}

# Function to create IAM user for GitHub Actions
create_github_actions_user() {
    print_header "Creating IAM User for GitHub Actions"
    
    # Create user
    if aws iam get-user --user-name "$IAM_USER_NAME" 2>/dev/null; then
        print_warning "IAM user $IAM_USER_NAME already exists"
    else
        aws iam create-user \
            --user-name "$IAM_USER_NAME" \
            --tags Key=Purpose,Value=GitHubActions Key=Project,Value=TicTacToe
        print_success "Created IAM user: $IAM_USER_NAME"
    fi
    
    # Create policy for GitHub Actions user
    cat > /tmp/github-actions-policy.json << EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "S3DeploymentUpload",
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:GetObject",
                "s3:ListBucket"
            ],
            "Resource": [
                "arn:aws:s3:::${BUCKET_NAME}",
                "arn:aws:s3:::${BUCKET_NAME}/*"
            ]
        },
        {
            "Sid": "SSMCommandExecution",
            "Effect": "Allow",
            "Action": [
                "ssm:SendCommand",
                "ssm:GetCommandInvocation",
                "ssm:ListCommandInvocations"
            ],
            "Resource": [
                "arn:aws:ec2:${REGION}:${ACCOUNT_ID}:instance/${INSTANCE_ID}",
                "arn:aws:ssm:${REGION}::document/AWS-RunShellScript"
            ]
        },
        {
            "Sid": "SSMDescribeInstances",
            "Effect": "Allow",
            "Action": [
                "ssm:DescribeInstanceInformation"
            ],
            "Resource": "*"
        }
    ]
}
EOF
    
    aws iam put-user-policy \
        --user-name "$IAM_USER_NAME" \
        --policy-name "GitHubActionsDeploymentPolicy" \
        --policy-document file:///tmp/github-actions-policy.json
    print_success "Attached deployment policy to user"
    
    rm /tmp/github-actions-policy.json
    
    # Create access key
    print_info "Creating access key for GitHub Actions..."
    ACCESS_KEY_OUTPUT=$(aws iam create-access-key --user-name "$IAM_USER_NAME" --output json)
    ACCESS_KEY_ID=$(echo "$ACCESS_KEY_OUTPUT" | grep -o '"AccessKeyId": "[^"]*' | cut -d'"' -f4)
    SECRET_ACCESS_KEY=$(echo "$ACCESS_KEY_OUTPUT" | grep -o '"SecretAccessKey": "[^"]*' | cut -d'"' -f4)
    
    print_success "Created access key"
}

# Function to print summary
print_summary() {
    print_header "Setup Complete! 🎉"
    
    echo -e "${GREEN}All AWS resources have been created successfully!${NC}\n"
    
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${YELLOW}GitHub Secrets Configuration${NC}"
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
    
    echo "Add these secrets to your GitHub repository:"
    echo "Go to: Settings → Secrets and variables → Actions → New repository secret"
    echo ""
    echo -e "${BLUE}AWS_ACCESS_KEY_ID:${NC}"
    echo "$ACCESS_KEY_ID"
    echo ""
    echo -e "${BLUE}AWS_SECRET_ACCESS_KEY:${NC}"
    echo "$SECRET_ACCESS_KEY"
    echo ""
    echo -e "${BLUE}AWS_REGION:${NC}"
    echo "$REGION"
    echo ""
    echo -e "${BLUE}EC2_INSTANCE_ID:${NC}"
    echo "$INSTANCE_ID"
    echo ""
    echo -e "${BLUE}DEPLOYMENT_S3_BUCKET:${NC}"
    echo "$BUCKET_NAME"
    echo ""
    
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${YELLOW}Resources Created${NC}"
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
    
    echo "✓ S3 Bucket: $BUCKET_NAME"
    echo "✓ IAM User: $IAM_USER_NAME"
    echo "✓ IAM Role: $IAM_ROLE_NAME"
    echo "✓ Instance Profile: $INSTANCE_PROFILE_NAME"
    echo ""
    
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${YELLOW}Next Steps${NC}"
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
    
    echo "1. Add the secrets above to your GitHub repository"
    echo "2. Restart your EC2 instance (optional, for SSM agent to pick up new permissions)"
    echo "3. Push to main branch to trigger the deployment"
    echo ""
    
    print_warning "IMPORTANT: Save the AWS_SECRET_ACCESS_KEY now - it won't be shown again!"
    echo ""
    
    # Save to file
    cat > /tmp/aws-deployment-secrets.txt << EOF
GitHub Secrets for TicTacToe Deployment
Generated: $(date)

AWS_ACCESS_KEY_ID=$ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY=$SECRET_ACCESS_KEY
AWS_REGION=$REGION
EC2_INSTANCE_ID=$INSTANCE_ID
DEPLOYMENT_S3_BUCKET=$BUCKET_NAME

Resources Created:
- S3 Bucket: $BUCKET_NAME
- IAM User: $IAM_USER_NAME
- IAM Role: $IAM_ROLE_NAME
- Instance Profile: $INSTANCE_PROFILE_NAME
EOF
    
    print_info "Secrets have been saved to: /tmp/aws-deployment-secrets.txt"
    print_warning "Delete this file after adding secrets to GitHub!"
}

# Main execution
main() {
    print_header "AWS Deployment Setup for TicTacToe"
    
    echo "This script will create the following AWS resources:"
    echo "  • S3 bucket for deployment files"
    echo "  • IAM role for EC2 instance"
    echo "  • IAM user for GitHub Actions"
    echo "  • Necessary policies and permissions"
    echo ""
    
    read -p "Do you want to continue? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_error "Setup cancelled"
        exit 1
    fi
    
    check_aws_cli
    check_aws_credentials
    get_region
    get_instance_id
    
    create_s3_bucket
    create_ec2_iam_role
    attach_role_to_instance
    create_github_actions_user
    
    print_summary
}

# Run main function
main
