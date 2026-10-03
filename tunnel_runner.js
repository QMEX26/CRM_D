const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');

const logPath = path.join(__dirname, 'tunnel.log');

function run() {
  const p = spawn('C:\\Windows\\System32\\OpenSSH\\ssh.exe', [
    '-o', 'StrictHostKeyChecking=no',
    '-o', 'ServerAliveInterval=15',
    '-o', 'ServerAliveCountMax=3',
    '-R', '80:localhost:8080',
    'nokey@localhost.run'
  ]);

  p.stdout.on('data', d => {
    fs.appendFileSync(logPath, d.toString());
  });

  p.stderr.on('data', d => {
    fs.appendFileSync(logPath, d.toString());
  });

  p.on('close', () => {
    fs.appendFileSync(logPath, '\n[Tunnel closed, restarting in 3s...]\n');
    setTimeout(run, 3000);
  });
}

fs.writeFileSync(logPath, '[Tunnel Runner Started]\n');
run();
