const { app, BrowserWindow, ipcMain, dialog, shell } = require('electron');
const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');
require('dotenv').config({ path: path.join(__dirname, '.env') });

let win;
let mvnExec = null;

function createWindow() {
  win = new BrowserWindow({
    width: 800,
    height: 510,
    resizable: false,
    title: 'LLM Mutator GUI',
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false
    }
  });

  win.loadFile('index.html');
}

app.whenReady().then(createWindow);

ipcMain.on('open-dialog', async (event) => {
  const result = await dialog.showOpenDialog(win, {
    properties: ['openFile', 'openDirectory'],
  });
  if (!result.canceled && result.filePaths.length > 0) {
    const selectedPath = result.filePaths[0];
    const stat = fs.statSync(selectedPath);
    if (stat.isFile()) {
      if (selectedPath.endsWith('.java')) {
        event.sender.send('selected-path', selectedPath);
      } else {
        dialog.showErrorBox('Invalid Selection', 'Select only a .java file or a repository containing .java files.');
      }
    } else {
      event.sender.send('selected-path', selectedPath);
    }
  }
});

ipcMain.on('run-command', (event, srcPath) => {
  let userArgs;

  try {
    const stat = fs.statSync(srcPath);
    userArgs = stat.isFile() ? ['-f', srcPath] : ['-dir', srcPath];
  } catch (err) {
    event.sender.send('command-output', `Error accessing path: ${err.message}`);
    return;
  }

  const jarPath = path.join(__dirname, '..', 'target', 'LLM_Mutator-jar-with-dependencies.jar');

  mvnExec = spawn('java', ['-jar', jarPath, ...userArgs], {
    shell: true,
  });

  mvnExec.stdout.on('data', data => {
    const lines = data.toString().split('\n');
    lines.forEach(line => {
      if (/^\r[|\/\\-]/.test(line)) {
        event.sender.send('command-output', line);
        return;
      }
      const trimmed = line.trim();
      event.sender.send('command-output', trimmed + '\n');
    });
  });

  mvnExec.stderr.on('data', data => {
    dialog.showErrorBox('Error', data.toString());
  });

  mvnExec.on('error', err => {
    dialog.showErrorBox('Error', err.message);
  });

  mvnExec.on('close', code => {
    event.sender.send('command-output', `\nMaven exec finished with code ${code}`);
    event.sender.send('process-complete');
    mvnExec = null;
  });
});

ipcMain.on('stop-process', () => {
  if (mvnExec) {
    mvnExec.kill('SIGTERM');
    mvnExec = null;
  }
});

ipcMain.on('resize-window', (event, newHeight) => {
  const win = BrowserWindow.getFocusedWindow();
  if (win) {
    const [currentWidth] = win.getSize();
    win.setSize(currentWidth, newHeight);
  }
});

ipcMain.on('open-destination', async (event, folderPath) => {
  const resolvedPath = path.resolve(folderPath);
  if (fs.existsSync(resolvedPath)) {
    const result = await shell.openPath(resolvedPath);
    if (result) {
      dialog.showErrorBox('Error Opening Folder', result);
    }
  } else {
    dialog.showErrorBox('Not Found', `The destination directory could not be found. It may not exist yet or may have been moved. Please run the tool to proceed.`);
  }
});

ipcMain.handle('get-app-version', () => {
  return app.getVersion();
});



