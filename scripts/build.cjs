const path = require('path');
const { execFileSync } = require('child_process');

const appRoot = path.resolve(__dirname, '..');

execFileSync(process.execPath, [path.join(appRoot, 'scripts', 'prepare-native.cjs')], {
  cwd: appRoot,
  stdio: 'inherit',
});

execFileSync(process.execPath, [path.join(appRoot, 'scripts', 'build-dev-client-bundle.cjs')], {
  cwd: appRoot,
  stdio: 'inherit',
});
