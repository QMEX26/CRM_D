try {
  require('dotenv').config();
} catch (e) {
  // dotenv loaded by Expo CLI
}

module.exports = ({ config }) => {
  const apiBaseUrl = process.env.API_BASE_URL || process.env.EXPO_PUBLIC_API_BASE_URL;

  return {
    ...config,
    extra: {
      ...config.extra,
      apiBaseUrl,
    },
  };
};
