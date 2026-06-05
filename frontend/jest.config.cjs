module.exports = {
  testEnvironment: 'jsdom',
  setupFilesAfterEnv: ['<rootDir>/src/setupTests.js'],
  moduleNameMapper: {
    '\\.(css|less|scss|sass)$': '<rootDir>/src/test/styleMock.js',
    '\\.(svg|png|jpg|jpeg|gif)$': '<rootDir>/src/test/fileMock.js',
  },
};
