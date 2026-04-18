const path = require('path');
const fs = require('fs');
const { execSync } = require('child_process');

const appRoot = path.resolve(__dirname, '..');
const devClientFromRepo = path.join(appRoot, '..', 'tamer-dev-client');
const devClientFromNode = path.join(appRoot, 'node_modules', '@tamer4lynx', 'tamer-dev-client');
const devClientFromRoot = path.join(appRoot, '..', '..', 'node_modules', '@tamer4lynx', 'tamer-dev-client');
const devClientDir = fs.existsSync(path.join(devClientFromRepo, 'package.json'))
  ? devClientFromRepo
  : fs.existsSync(path.join(devClientFromNode, 'package.json'))
    ? devClientFromNode
    : devClientFromRoot;

if (!fs.existsSync(path.join(devClientDir, 'package.json'))) {
  console.error('@tamer4lynx/tamer-dev-client not found. Install it or run from monorepo root.');
  process.exit(1);
}

console.log('Building tamer-dev-client...');
execSync('npm run build', { stdio: 'inherit', cwd: devClientDir });

const bundles = ['dev-client.lynx.bundle', 'tamer-debug.lynx.bundle'];
const assetsDir = path.join(appRoot, 'android', 'app', 'src', 'main', 'assets');
fs.mkdirSync(assetsDir, { recursive: true });
for (const name of bundles) {
  const bundlePath = path.join(devClientDir, 'dist', name);
  if (!fs.existsSync(bundlePath)) {
    console.error('Bundle not found at', bundlePath);
    process.exit(1);
  }
  fs.copyFileSync(bundlePath, path.join(assetsDir, name));
  console.log(`Copied ${name} to android assets.`);
}
