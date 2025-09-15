import React, { useState } from 'react';

const File = ({ file, onFileSelect, selectedFile, onActionSelect, selectedForAction }) => (
  <div
    className={`file-entry ${selectedFile === file.path ? 'selected' : ''} ${selectedForAction?.path === file.path ? 'action-selected' : ''}`}
    onClick={() => {
      onFileSelect(file.path);
      onActionSelect({ path: file.path, isDirectory: false });
    }}
  >
    {file.name}
  </div>
);

const Directory = ({ directory, onFileSelect, selectedFile, fetchFiles, renderFileTree, onActionSelect, selectedForAction }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [children, setChildren] = useState([]);

  const handleHeaderClick = async (e) => {
    e.stopPropagation();
    onActionSelect({ path: directory.path, isDirectory: true });
    const newIsOpen = !isOpen;
    setIsOpen(newIsOpen);
    if (newIsOpen && children.length === 0) {
      const subFiles = await fetchFiles(directory.path);
      setChildren(subFiles);
    }
  };

  return (
    <div className={`dir-entry ${isOpen ? 'open' : ''}`}>
      <div 
        className={`dir-header ${selectedForAction?.path === directory.path ? 'selected' : ''}`}
        onClick={handleHeaderClick}
      >
        {directory.name}
      </div>
      <div className="dir-content">
        {isOpen && renderFileTree(children, onFileSelect, selectedFile, fetchFiles, onActionSelect, selectedForAction)}
      </div>
    </div>
  );
};

function FileBrowser({ files, onFileSelect, selectedFile, fetchFiles, refreshFileBrowser }) {
  const [selectedForAction, setSelectedForAction] = useState(null);

  const renderFileTree = (files, onFileSelect, selectedFile, fetchFiles, onActionSelect, selectedForAction) => {
    return files
      .sort((a, b) => {
        if (a.isDirectory !== b.isDirectory) {
          return a.isDirectory ? -1 : 1;
        }
        return a.name.localeCompare(b.name);
      })
      .map(file => {
        if (file.isDirectory) {
          return (
            <Directory
              key={file.path}
              directory={file}
              onFileSelect={onFileSelect}
              selectedFile={selectedFile}
              fetchFiles={fetchFiles}
              renderFileTree={renderFileTree}
              onActionSelect={onActionSelect}
              selectedForAction={selectedForAction}
            />
          );
        }
        return (
          <File
            key={file.path}
            file={file}
            onFileSelect={onFileSelect}
            selectedFile={selectedFile}
            onActionSelect={onActionSelect}
            selectedForAction={selectedForAction}
          />
        );
      });
  };
  
  const handleCreateFile = () => {
    const fileName = prompt('Enter file name (e.g., new-file.md):');
    if (fileName && fileName.trim()) {
      const currentDir = selectedForAction?.isDirectory ? selectedForAction.path : '/';
      const newPath = (currentDir === '/' ? '' : currentDir) + '/' + fileName.trim();
      fetch(`/create-file?path=${encodeURIComponent(newPath)}`, { method: 'POST' }).then(() => {
        refreshFileBrowser();
        setSelectedForAction(null);
      });
    }
  };

  const handleCreateDir = () => {
    const dirName = prompt('Enter directory name:');
    if (dirName && dirName.trim()) {
      const currentDir = selectedForAction?.isDirectory ? selectedForAction.path : '/';
      const newPath = (currentDir === '/' ? '' : currentDir) + '/' + dirName.trim();
      fetch(`/create-directory?path=${encodeURIComponent(newPath)}`, { method: 'POST' }).then(() => {
        refreshFileBrowser();
        setSelectedForAction(null);
      });
    }
  };

  const handleDelete = () => {
    if (!selectedForAction) {
      alert('Please select a file or directory to delete.');
      return;
    }

    const pathToDelete = selectedForAction.path;
    if (confirm(`Are you sure you want to delete '${pathToDelete}'?`)) {
      fetch(`/delete?path=${encodeURIComponent(pathToDelete)}`, { method: 'DELETE' })
        .then(response => {
          if (!response.ok) {
            alert('Delete failed!');
          } else {
            if (pathToDelete === selectedFile) {
              onFileSelect(null);
            }
            setSelectedForAction(null);
            refreshFileBrowser();
          }
        });
    }
  };

  return (
    <div className="sidebar">
      <div className="sidebar-header">
        <h3>Files</h3>
        <div className="sidebar-actions">
          <button onClick={handleCreateFile} title="Create new file">📄</button>
          <button onClick={handleCreateDir} title="Create new directory">📁</button>
          <button onClick={handleDelete} title="Delete selected">🗑️</button>
        </div>
      </div>
      <div id="file-browser" className="file-browser">
        {renderFileTree(files, onFileSelect, selectedFile, fetchFiles, setSelectedForAction, selectedForAction)}
      </div>
    </div>
  );
}

export default FileBrowser;
