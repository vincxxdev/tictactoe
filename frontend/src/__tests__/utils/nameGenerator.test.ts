import { adjectives, nouns } from '../../utils/nameGenerator';

describe('nameGenerator', () => {
  describe('adjectives', () => {
    test('should contain expected adjectives', () => {
      expect(adjectives).toContain('Swift');
      expect(adjectives).toContain('Agile');
      expect(adjectives).toContain('Clever');
      expect(adjectives).toContain('Mighty');
      expect(adjectives).toContain('Wise');
    });

    test('should have 10 adjectives', () => {
      expect(adjectives).toHaveLength(10);
    });

    test('all adjectives should be strings', () => {
      adjectives.forEach(adj => {
        expect(typeof adj).toBe('string');
      });
    });

    test('all adjectives should be capitalized', () => {
      adjectives.forEach(adj => {
        expect(adj[0]).toBe(adj[0].toUpperCase());
      });
    });

    test('should not contain duplicate adjectives', () => {
      const uniqueAdjectives = new Set(adjectives);
      expect(uniqueAdjectives.size).toBe(adjectives.length);
    });
  });

  describe('nouns', () => {
    test('should contain expected nouns', () => {
      expect(nouns).toContain('Lion');
      expect(nouns).toContain('Tiger');
      expect(nouns).toContain('Bear');
      expect(nouns).toContain('Eagle');
      expect(nouns).toContain('Fox');
    });

    test('should have 10 nouns', () => {
      expect(nouns).toHaveLength(10);
    });

    test('all nouns should be strings', () => {
      nouns.forEach(noun => {
        expect(typeof noun).toBe('string');
      });
    });

    test('all nouns should be capitalized', () => {
      nouns.forEach(noun => {
        expect(noun[0]).toBe(noun[0].toUpperCase());
      });
    });

    test('should not contain duplicate nouns', () => {
      const uniqueNouns = new Set(nouns);
      expect(uniqueNouns.size).toBe(nouns.length);
    });
  });

  describe('name generation', () => {
    test('can generate random name combinations', () => {
      const randomAdjective = adjectives[Math.floor(Math.random() * adjectives.length)];
      const randomNoun = nouns[Math.floor(Math.random() * nouns.length)];
      const randomName = `${randomAdjective} ${randomNoun}`;

      expect(randomName).toMatch(/^[A-Z][a-z]+ [A-Z][a-z]+$/);
    });

    test('adjectives and nouns arrays are not empty', () => {
      expect(adjectives.length).toBeGreaterThan(0);
      expect(nouns.length).toBeGreaterThan(0);
    });

    test('all possible combinations are valid', () => {
      adjectives.forEach(adj => {
        nouns.forEach(noun => {
          const name = `${adj} ${noun}`;
          expect(name).toMatch(/^[A-Z][a-z]+ [A-Z][a-z]+$/);
        });
      });
    });
  });
});

