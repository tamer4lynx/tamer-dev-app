const path = require('path');
const fs = require('fs');
const { execFileSync } = require('child_process');

const appRoot = path.resolve(__dirname, '..');
const devClientFromRepo = path.join(appRoot, '..', 'tamer-dev-client');
const devClientFromNode = path.join(appRoot, 'node_modules', '@tamer4lynx', 'tamer-dev-client');
const devClientFromRoot = path.join(appRoot, '..', '..', 'node_modules', '@tamer4lynx', 'tamer-dev-client');
const devClientDir = fs.existsSync(path.join(devClientFromRepo, 'package.json'))
  ? devClientFromRepo
  : fs.existsSync(path.join(devClientFromNode, 'package.json'))
    ? devClientFromNode
    : devClientFromRoot;

function readOfficialMetadata() {
  const metadataPath = path.join(appRoot, 'official-app.json');
  const metadata = fs.existsSync(metadataPath)
    ? JSON.parse(fs.readFileSync(metadataPath, 'utf8'))
    : {};
  if (process.env.OFFICIAL_APP_SOURCE) {
    metadata.source = process.env.OFFICIAL_APP_SOURCE;
  }
  return metadata;
}

function copyDirectoryContents(sourceDir, destDir) {
  fs.mkdirSync(destDir, { recursive: true });
  for (const entry of fs.readdirSync(sourceDir, { withFileTypes: true })) {
    const sourcePath = path.join(sourceDir, entry.name);
    const destPath = path.join(destDir, entry.name);
    if (entry.isDirectory()) {
      copyDirectoryContents(sourcePath, destPath);
    } else if (entry.isFile()) {
      fs.mkdirSync(path.dirname(destPath), { recursive: true });
      fs.copyFileSync(sourcePath, destPath);
    }
  }
}

function cleanKnownDistOutputs(destDir) {
  for (const name of ['dev-client.lynx.bundle', 'tamer-debug.lynx.bundle', 'tamer-assets.json', 'static']) {
    fs.rmSync(path.join(destDir, name), { recursive: true, force: true });
  }
}

function copyDistToNative(distDir) {
  const targets = [
    path.join(appRoot, 'android', 'app', 'src', 'main', 'assets'),
    path.join(appRoot, 'ios', 'TamerDevApp'),
    path.join(appRoot, 'dist'),
  ];

  for (const target of targets) {
    if (!fs.existsSync(target)) {
      fs.mkdirSync(target, { recursive: true });
    }
    cleanKnownDistOutputs(target);
    copyDirectoryContents(distDir, target);
    console.log(`Copied dev-client dist to ${path.relative(appRoot, target)}.`);
  }
}

if (!fs.existsSync(path.join(devClientDir, 'package.json'))) {
  console.error('@tamer4lynx/tamer-dev-client not found. Install it or run from monorepo root.');
  process.exit(1);
}

const metadata = readOfficialMetadata();
const env = {
  ...process.env,
  TAMER_DEV_CLIENT_OFFICIAL_APP_METADATA_JSON: JSON.stringify(metadata),
};

console.log('Building tamer-dev-client for Tamer Dev App...');
execFileSync('npm', ['run', 'build'], { stdio: 'inherit', cwd: devClientDir, env });

const distDir = path.join(devClientDir, 'dist');
for (const name of ['dev-client.lynx.bundle', 'tamer-debug.lynx.bundle']) {
  const bundlePath = path.join(distDir, name);
  if (!fs.existsSync(bundlePath)) {
    console.error('Bundle not found at', bundlePath);
    process.exit(1);
  }
}

copyDistToNative(distDir);
