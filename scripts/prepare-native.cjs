const path = require('path');
const fs = require('fs');
const { execFileSync } = require('child_process');

const appRoot = path.resolve(__dirname, '..');
const repoRoot = path.resolve(appRoot, '..', '..');
const cliPath = path.join(repoRoot, 'dist', 'index.js');
const metadataPath = path.join(appRoot, 'official-app.json');

function readOfficialMetadata() {
  const metadata = fs.existsSync(metadataPath)
    ? JSON.parse(fs.readFileSync(metadataPath, 'utf8'))
    : {};
  if (process.env.OFFICIAL_APP_SOURCE) {
    metadata.source = process.env.OFFICIAL_APP_SOURCE;
  }
  return metadata;
}

function runCli(args, env) {
  execFileSync(process.execPath, [cliPath, ...args], {
    cwd: appRoot,
    env,
    stdio: 'inherit',
  });
}

if (!fs.existsSync(cliPath)) {
  console.log('Building local t4l CLI before preparing Tamer Dev App native projects...');
  execFileSync('npm', ['run', 'build'], { cwd: repoRoot, stdio: 'inherit' });
}

const env = {
  ...process.env,
  TAMER_DEV_CLIENT_OFFICIAL_APP_METADATA_JSON: JSON.stringify(readOfficialMetadata()),
};

fs.rmSync(path.join(appRoot, 'android'), {
  recursive: true,
  force: true,
  maxRetries: 5,
  retryDelay: 100,
});
fs.rmSync(path.join(appRoot, 'ios'), {
  recursive: true,
  force: true,
  maxRetries: 5,
  retryDelay: 100,
});

runCli(['android', 'create', '--release'], env);
runCli(['sync', 'ios'], env);
runCli(['link'], env);
