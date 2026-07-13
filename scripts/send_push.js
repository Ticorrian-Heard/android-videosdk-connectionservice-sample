const { exec } = require('child_process');

// Usage: node send_push.js [--action ACTION] [--extraKey KEY --extraValue VALUE]
// Default action: com.videosdkconnectionservice.ACTION_START_CALL

const args = process.argv.slice(2);
let action = 'com.videosdkconnectionservice.ACTION_START_CALL';
let extraKey = null;
let extraValue = null;

for (let i = 0; i < args.length; i++) {
  const a = args[i];
  if (a === '--action' && args[i+1]) { action = args[i+1]; i++; }
  if (a === '--extraKey' && args[i+1]) { extraKey = args[i+1]; i++; }
  if (a === '--extraValue' && args[i+1]) { extraValue = args[i+1]; i++; }
}

let cmd = `adb shell am broadcast -a ${action}`;
if (extraKey && extraValue) {
  // send string extra
  cmd += ` --es ${extraKey} \"${extraValue}\"`;
}

console.log('Running:', cmd);
exec(cmd, (err, stdout, stderr) => {
  if (err) {
    console.error('Error executing adb broadcast:', err);
    return;
  }
  console.log('stdout:', stdout);
  console.error('stderr:', stderr);
});

