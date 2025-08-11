document.addEventListener('DOMContentLoaded', () => {
    const fileBrowser = document.getElementById('file-browser');
    const editorContainer = document.getElementById('editor-container');
    const editorPane = document.getElementById('editor-pane');
    const previewPane = document.getElementById('preview-pane');
    const welcomeScreen = document.getElementById('welcome-screen');
    const createFileBtn = document.getElementById('create-file-btn');
    const createDirBtn = document.getElementById('create-dir-btn');

    let selectedFile = null;
    let currentFileContent = '';
    const converter = new showdown.Converter({ tables: true, strikethrough: true, tasklists: true, openLinksInNewWindow: true });

    async function fetchFiles(path) {
        try {
            const response = await fetch(`/list?path=${encodeURIComponent(path)}`);
            if (!response.ok) throw new Error('Failed to fetch files');
            return await response.json();
        } catch (error) {
            console.error('Fetch files error:', error);
            return [];
        }
    }

    function renderFileTree(files, container) {
        container.innerHTML = ''; // Clear previous contents
        files.sort((a, b) => {
            if (a.isDirectory !== b.isDirectory) return a.isDirectory ? -1 : 1;
            return a.name.localeCompare(b.name);
        });

        for (const file of files) {
            if (file.isDirectory) {
                const dirEntry = document.createElement('div');
                dirEntry.className = 'dir-entry';
                dirEntry.dataset.path = file.path;
                dirEntry.dataset.isDirectory = 'true';

                const dirHeader = document.createElement('div');
                dirHeader.className = 'dir-header';
                dirHeader.textContent = file.name;
                dirHeader.draggable = true;

                const dirContent = document.createElement('div');
                dirContent.className = 'dir-content';

                dirEntry.appendChild(dirHeader);
                dirEntry.appendChild(dirContent);

                dirHeader.addEventListener('click', (e) => {
                    e.stopPropagation();
                    const isOpen = dirEntry.classList.toggle('open');
                    if (isOpen && !dirContent.hasChildNodes()) {
                        fetchFiles(file.path).then(subFiles => renderFileTree(subFiles, dirContent));
                    }
                });
                container.appendChild(dirEntry);
            } else {
                const fileEntry = document.createElement('div');
                fileEntry.className = 'file-entry';
                fileEntry.textContent = file.name;
                fileEntry.dataset.path = file.path;
                fileEntry.dataset.isDirectory = 'false';
                fileEntry.draggable = true;

                fileEntry.addEventListener('click', (e) => {
                    e.stopPropagation();
                    selectFile(file.path, fileEntry);
                });
                container.appendChild(fileEntry);
            }
        }
    }

    function selectFile(path, element) {
        if (selectedFile) {
            const oldSelected = document.querySelector(`.file-entry.selected`);
            if (oldSelected) oldSelected.classList.remove('selected');
        }
        selectedFile = path;
        element.classList.add('selected');

        welcomeScreen.style.display = 'none';
        editorContainer.style.display = 'flex';
        loadAndEditFile(path);
    }

    async function loadAndEditFile(path) {
        try {
            const response = await fetch(`/raw?path=${encodeURIComponent(path)}`);
            const content = await response.text();
            currentFileContent = content;
            editorPane.value = content;
            previewPane.innerHTML = converter.makeHtml(content);
        } catch (error) {
            console.error('Error loading file:', error);
        }
    }

    editorPane.addEventListener('input', () => {
        const newContent = editorPane.value;
        previewPane.innerHTML = converter.makeHtml(newContent);
        if (newContent !== currentFileContent) {
            currentFileContent = newContent;
            if (selectedFile) {
                // Debounce save operation
                clearTimeout(saveFile.timeout);
                saveFile.timeout = setTimeout(() => saveFile(selectedFile, newContent), 500);
            }
        }
    });

    async function saveFile(path, content) {
        try {
            await fetch(`/save?path=${encodeURIComponent(path)}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: `content=${encodeURIComponent(content)}`
            });
        } catch (error) {
            console.error('Error saving file:', error);
        }
    }

    function getCurrentDirectory() {
        if (!selectedFile) return '/';
        const selectedElement = document.querySelector('.file-entry.selected');
        const parentDir = selectedElement ? selectedElement.closest('.dir-entry') : null;
        return parentDir ? parentDir.dataset.path : '/';
    }

    createFileBtn.addEventListener('click', () => {
        const fileName = prompt('Enter file name (e.g., new-file.md):');
        if (fileName && fileName.trim()) {
            const currentDir = getCurrentDirectory();
            const newPath = (currentDir === '/' ? '' : currentDir) + '/' + fileName.trim();
            fetch(`/create-file?path=${encodeURIComponent(newPath)}`, { method: 'POST' }).then(refreshFileBrowser);
        }
    });

    createDirBtn.addEventListener('click', () => {
        const dirName = prompt('Enter directory name:');
        if (dirName && dirName.trim()) {
            const currentDir = getCurrentDirectory();
            const newPath = (currentDir === '/' ? '' : currentDir) + '/' + dirName.trim();
            fetch(`/create-directory?path=${encodeURIComponent(newPath)}`, { method: 'POST' }).then(refreshFileBrowser);
        }
    });

    // --- Drag and Drop --- //
    let draggedItem = null;

    fileBrowser.addEventListener('dragstart', (e) => {
        const target = e.target.closest('.file-entry, .dir-header');
        if (target) {
            draggedItem = target;
            const path = target.classList.contains('dir-header') ? target.parentElement.dataset.path : target.dataset.path;
            e.dataTransfer.effectAllowed = 'move';
            e.dataTransfer.setData('text/plain', path);
            setTimeout(() => { (target.classList.contains('dir-header') ? target.parentElement : target).style.opacity = '0.5'; }, 0);
        }
    });

    fileBrowser.addEventListener('dragend', (e) => {
        if (draggedItem) {
            const el = draggedItem.classList.contains('dir-header') ? draggedItem.parentElement : draggedItem;
            el.style.opacity = '';
            draggedItem = null;
        }
    });

    fileBrowser.addEventListener('dragover', (e) => {
        e.preventDefault();
    });

    fileBrowser.addEventListener('drop', (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (!draggedItem) return;

        const sourcePath = e.dataTransfer.getData('text/plain');
        let destinationDirPath;

        const targetDir = e.target.closest('.dir-entry');
        if (targetDir) {
            destinationDirPath = targetDir.dataset.path;
        } else {
            destinationDirPath = '/'; // Dropped in the root area
        }

        const sourceParentDir = sourcePath.substring(0, sourcePath.lastIndexOf('/')) || '/';

        if (sourcePath && destinationDirPath && sourceParentDir !== destinationDirPath && !destinationDirPath.startsWith(sourcePath + '/')) {
            fetch(`/move?source=${encodeURIComponent(sourcePath)}&destination=${encodeURIComponent(destinationDirPath)}`, { method: 'POST' })
                .then(response => {
                    if (!response.ok) alert('Move failed!');
                    refreshFileBrowser();
                });
        }
    });

    function refreshFileBrowser() {
        const openDirs = [...document.querySelectorAll('.dir-entry.open')].map(el => el.dataset.path);
        fetchFiles('/').then(files => {
            renderFileTree(files, fileBrowser);
            openDirs.forEach(path => {
                const dirEntry = document.querySelector(`.dir-entry[data-path="${path}"]`);
                if (dirEntry) {
                    dirEntry.classList.add('open');
                    const dirContent = dirEntry.querySelector('.dir-content');
                    if (dirContent) {
                        fetchFiles(path).then(subFiles => renderFileTree(subFiles, dirContent));
                    }
                }
            });
        });
    }

    // Initial load
    refreshFileBrowser();
});
