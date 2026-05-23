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

// Patch versionName in build.gradle.kts to match package.json version
const pkg = JSON.parse(fs.readFileSync(path.join(appRoot, 'package.json'), 'utf8'));
const appVersion = pkg.version ?? '1.0';
const gradlePath = path.join(appRoot, 'android', 'app', 'build.gradle.kts');
if (fs.existsSync(gradlePath)) {
  let gradle = fs.readFileSync(gradlePath, 'utf8');
  gradle = gradle.replace(/versionName = ".*?"/, `versionName = "${appVersion}"`);
  fs.writeFileSync(gradlePath, gradle);
  console.log(`✅ Patched Android versionName → ${appVersion}`);
}

runCli(['sync', 'ios'], env);

// Patch iOS MARKETING_VERSION and CFBundleShortVersionString
const pbxprojPath = path.join(appRoot, 'ios', 'TamerDevApp.xcodeproj', 'project.pbxproj');
if (fs.existsSync(pbxprojPath)) {
  let pbx = fs.readFileSync(pbxprojPath, 'utf8');
  pbx = pbx.replace(/MARKETING_VERSION = ".*?";/g, `MARKETING_VERSION = "${appVersion}";`);
  fs.writeFileSync(pbxprojPath, pbx);
}
const infoPlistPath = path.join(appRoot, 'ios', 'TamerDevApp', 'Info.plist');
if (fs.existsSync(infoPlistPath)) {
  let plist = fs.readFileSync(infoPlistPath, 'utf8');
  plist = plist.replace(
    /(<key>CFBundleShortVersionString<\/key>\s*<string>)[^<]*(<\/string>)/,
    `$1${appVersion}$2`
  );
  plist = plist.replace(
    /(<key>CFBundleVersion<\/key>\s*<string>)[^<]*(<\/string>)/,
    `$1${appVersion}$2`
  );
  fs.writeFileSync(infoPlistPath, plist);
  console.log(`✅ Patched iOS version → ${appVersion}`);
}

runCli(['link'], env);
