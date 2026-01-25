export default {
  extends: ['@commitlint/config-conventional'],
  rules: {
    // Scope 제외
    'scope-empty': [2, 'always'],

    // Subject (description) 규칙
    'subject-max-length': [2, 'always', 50],
    'subject-full-stop': [2, 'never', '.'],
    'subject-case': [0], // 한글 허용을 위해 case 검사 비활성화

    // Body 규칙
    'body-leading-blank': [2, 'always'],
    'body-max-line-length': [0],

    // Footer 비활성화
    'footer-leading-blank': [0],
    'footer-max-line-length': [0],

    // 커스텀 규칙 적용
    'body-starts-with-dash': [2, 'always'],
  },
  plugins: [
    {
      rules: {
        'body-starts-with-dash': (parsed) => {
          const { body } = parsed;

          if (!body) {
            return [true];
          }

          const lines = body
            .split('\n')
            .filter((line) => line.trim().length > 0);
          const allStartWithDash = lines.every((line) =>
            line.trim().startsWith('-'),
          );

          if (!allStartWithDash) {
            return [false, 'body의 각 줄은 "-"로 시작해야 합니다'];
          }

          return [true];
        },
      },
    },
  ],
};