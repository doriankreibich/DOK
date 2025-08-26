document.addEventListener('DOMContentLoaded', () => {
    const fileBrowser = document.getElementById('file-browser');
    const editorContainer = document.getElementById('editor-container');
    const editorPane = document.getElementById('editor-pane');
    const previewPane = document.getElementById('preview-pane');
    const welcomeScreen = document.getElementById('welcome-screen');
    const createFileBtn = document.getElementById('create-file-btn');
    const createDirBtn = document.getElementById('create-dir-btn');
    const deleteBtn = document.getElementById('delete-btn');

    let selectedFile = null;
    let saveTimeout = null;
    const converter = new showdown.Converter({ tables: true, strikethrough: true, tasklists: true, openLinksInNewWindow: true });

    /**
     * Fetches the file list for a given directory path from the server.
     */
    async function fetchFiles(path) {
        try {
            const response = await fetch(`/list?path=${encodeURIComponent(path)}`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            return await response.json();
        } catch (error) {
            console.error('Failed to fetch file tree:', error);
            return [];
        }
    }

    /**
     * Renders the file and directory tree in the specified container element.
     */
    function renderFileTree(files, container) {
        container.innerHTML = '';
        files.sort((a, b) => {
            if (a.isDirectory !== b.isDirectory) return a.isDirectory ? -1 : 1;
            return a.name.localeCompare(b.name);
        });

        files.forEach(file => {
            if (file.isDirectory) {
                const dirEntry = createDirElement(file);
                container.appendChild(dirEntry);
            } else {
                const fileEntry = createFileElement(file);
                container.appendChild(fileEntry);
            }
        });
    }

    /**
     * Loads a file's content into the editor and updates the preview.
     */
    async function loadFile(path) {
        try {
            const response = await fetch(`/raw?path=${encodeURIComponent(path)}`);
            const content = await response.text();
            editorPane.value = content;
            previewPane.innerHTML = converter.makeHtml(content);
            selectedFile = path;

            welcomeScreen.style.display = 'none';
            editorContainer.style.display = 'flex';
        } catch (error) {
            console.error('Error loading file:', error);
        }
    }

    /**
     * Saves the provided content to the specified file path.
     */
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

    /**
     * Refreshes the entire file browser, preserving the state of open directories.
     */
    function refreshFileBrowser() {
        const openDirs = [...document.querySelectorAll('.dir-entry.open')].map(el => el.dataset.path);
        fetchFiles('/').then(files => {
            renderFileTree(files, fileBrowser);
            openDirs.forEach(path => {
                const dirEntry = document.querySelector(`.dir-entry[data-path="${path}"]`);
                if (dirEntry) {
                    dirEntry.classList.add('open');
                    const dirContent = dirEntry.querySelector('.dir-content');
                    if (dirContent && !dirContent.hasChildNodes()) {
                        fetchFiles(path).then(subFiles => renderFileTree(subFiles, dirContent));
                    }
                }
            });
        });
    }

    function createFileElement(file) {
        const fileEntry = document.createElement('div');
        fileEntry.className = 'file-entry';
        fileEntry.textContent = file.name;
        fileEntry.dataset.path = file.path;
        fileEntry.draggable = true;
        fileEntry.addEventListener('click', (e) => {
            e.stopPropagation();
            document.querySelectorAll('.file-entry.selected, .dir-header.selected').forEach(el => el.classList.remove('selected'));
            fileEntry.classList.add('selected');
            loadFile(file.path);
        });
        return fileEntry;
    }

    function createDirElement(file) {
        const dirEntry = document.createElement('div');
        dirEntry.className = 'dir-entry';
        dirEntry.dataset.path = file.path;

        const dirHeader = document.createElement('div');
        dirHeader.className = 'dir-header';
        dirHeader.textContent = file.name;
        dirHeader.draggable = true;

        const dirContent = document.createElement('div');
        dirContent.className = 'dir-content';

        dirHeader.addEventListener('click', (e) => {
            e.stopPropagation();
            document.querySelectorAll('.file-entry.selected, .dir-header.selected').forEach(el => el.classList.remove('selected'));
            dirHeader.classList.add('selected');
            const isOpen = dirEntry.classList.toggle('open');
            if (isOpen && !dirContent.hasChildNodes()) {
                fetchFiles(file.path).then(subFiles => renderFileTree(subFiles, dirContent));
            }
        });

        dirEntry.appendChild(dirHeader);
        dirEntry.appendChild(dirContent);
        return dirEntry;
    }

    editorPane.addEventListener('input', () => {
        const newContent = editorPane.value;
        previewPane.innerHTML = converter.makeHtml(newContent);
        
        clearTimeout(saveTimeout);
        if (selectedFile) {
            saveTimeout = setTimeout(() => saveFile(selectedFile, newContent), 500);
        }
    });

    createFileBtn.addEventListener('click', () => {
        const fileName = prompt('Enter file name (e.g., new-file.md):');
        if (fileName && fileName.trim()) {
            const currentDir = document.querySelector('.dir-header.selected')?.parentElement.dataset.path || '/';
            const newPath = (currentDir === '/' ? '' : currentDir) + '/' + fileName.trim();
            fetch(`/create-file?path=${encodeURIComponent(newPath)}`, { method: 'POST' }).then(refreshFileBrowser);
        }
    });

    createDirBtn.addEventListener('click', () => {
        const dirName = prompt('Enter directory name:');
        if (dirName && dirName.trim()) {
            const currentDir = document.querySelector('.dir-header.selected')?.parentElement.dataset.path || '/';
            const newPath = (currentDir === '/' ? '' : currentDir) + '/' + dirName.trim();
            fetch(`/create-directory?path=${encodeURIComponent(newPath)}`, { method: 'POST' }).then(refreshFileBrowser);
        }
    });

    deleteBtn.addEventListener('click', () => {
        const selectedElement = document.querySelector('.file-entry.selected, .dir-header.selected');
        if (!selectedElement) {
            alert('Please select a file or directory to delete.');
            return;
        }

        const pathToDelete = selectedElement.classList.contains('dir-header') 
            ? selectedElement.parentElement.dataset.path 
            : selectedElement.dataset.path;

        if (confirm(`Are you sure you want to delete '${pathToDelete}'?`)) {
            fetch(`/delete?path=${encodeURIComponent(pathToDelete)}`, { method: 'DELETE' })
                .then(response => {
                    if (!response.ok) {
                        alert('Delete failed!');
                    } else {
                        if (pathToDelete === selectedFile) {
                            editorPane.value = '';
                            previewPane.innerHTML = '';
                            selectedFile = null;
                            editorContainer.style.display = 'none';
                            welcomeScreen.style.display = 'block';
                        }
                        refreshFileBrowser();
                    }
                });
        }
    });

    let draggedItemPath = null;
    fileBrowser.addEventListener('dragstart', (e) => {
        const target = e.target.closest('.file-entry, .dir-header');
        if (target) {
            draggedItemPath = target.classList.contains('dir-header') ? target.parentElement.dataset.path : target.dataset.path;
            e.dataTransfer.effectAllowed = 'move';
            e.dataTransfer.setData('text/plain', draggedItemPath);
        }
    });

    fileBrowser.addEventListener('dragover', (e) => e.preventDefault());

    fileBrowser.addEventListener('drop', (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (!draggedItemPath) return;

        const destinationDir = e.target.closest('.dir-entry');
        const destinationPath = destinationDir ? destinationDir.dataset.path : '/';

        if (draggedItemPath && destinationPath && !destinationPath.startsWith(draggedItemPath + '/')) {
            fetch(`/move?source=${encodeURIComponent(draggedItemPath)}&destination=${encodeURIComponent(destinationPath)}`, { method: 'POST' })
                .then(response => {
                    if (!response.ok) alert('Move failed!');
                    refreshFileBrowser();
                });
        }
        draggedItemPath = null;
    });

    refreshFileBrowser();
});