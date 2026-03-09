const path = require('path');
const fs = require('fs');
const { execSync } = require('child_process');

const appRoot = path.resolve(__dirname, '..');
const devClientFromNode = path.join(appRoot, 'node_modules', 'tamer-dev-client');
const devClientFromRepo = path.join(appRoot, '..', 'tamer-dev-client');
const devClientDir = fs.existsSync(path.join(devClientFromNode, 'package.json'))
  ? devClientFromNode
  : devClientFromRepo;

if (!fs.existsSync(path.join(devClientDir, 'package.json'))) {
  console.error('tamer-dev-client not found. Install it or run from monorepo root.');
  process.exit(1);
}

console.log('Building tamer-dev-client...');
execSync('npm run build', { stdio: 'inherit', cwd: devClientDir });

const bundlePath = path.join(devClientDir, 'dist', 'dev-client.lynx.bundle');
const assetsDir = path.join(appRoot, 'android', 'app', 'src', 'main', 'assets');
if (!fs.existsSync(bundlePath)) {
  console.error('Dev client bundle not found at', bundlePath);
  process.exit(1);
}
fs.mkdirSync(assetsDir, { recursive: true });
fs.copyFileSync(bundlePath, path.join(assetsDir, 'dev-client.lynx.bundle'));
console.log('Copied dev-client.lynx.bundle to android assets.');
