import React, { useState, useEffect } from 'react';
import FileBrowser from './FileBrowser';
import Editor from './Editor';
import Preview from './Preview';
import './App.css';

function App() {
  const [files, setFiles] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [editorContent, setEditorContent] = useState('');
  const [saveTimeout, setSaveTimeout] = useState(null);

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

  const refreshFileBrowser = () => {
    fetchFiles('/').then(setFiles);
  };

  useEffect(() => {
    refreshFileBrowser();
  }, []);

  async function loadFile(path) {
    try {
      const response = await fetch(`/raw?path=${encodeURIComponent(path)}`);
      const content = await response.text();
      setEditorContent(content);
    } catch (error) {
      console.error('Error loading file:', error);
    }
  }

  async function saveFile(path, content) {
    try {
      await fetch('/save', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ path: path, content: content })
      });
    } catch (error) {
      console.error('Error saving file:', error);
    }
  }

  useEffect(() => {
    if (selectedFile) {
      loadFile(selectedFile);
    } else {
      setEditorContent(''); // Clear editor if no file is selected
    }
  }, [selectedFile]);

  const handleFileSelect = (path) => {
    setSelectedFile(path);
  };

  const handleEditorChange = (newContent) => {
    setEditorContent(newContent);
    
    clearTimeout(saveTimeout);
    if (selectedFile) {
      const timeout = setTimeout(() => saveFile(selectedFile, newContent), 500);
      setSaveTimeout(timeout);
    }
  };

  return (
    <div className="container">
      <FileBrowser 
        files={files} 
        onFileSelect={handleFileSelect} 
        selectedFile={selectedFile}
        fetchFiles={fetchFiles}
        refreshFileBrowser={refreshFileBrowser}
      />
      <div className="main-content">
        {selectedFile ? (
          <div id="editor-container">
            <Editor content={editorContent} onChange={handleEditorChange} />
            <Preview content={editorContent} />
          </div>
        ) : (
          <div id="welcome-screen">
            <h1>Welcome to DOK</h1>
            <p>Select a file to start editing or create a new one.</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
