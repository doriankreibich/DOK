import React from 'react';

function Editor({ content, onChange }) {
  const handleChange = (e) => {
    onChange(e.target.value);
  };

  return (
    <textarea
      id="editor-pane"
      className="editor-pane"
      value={content}
      onChange={handleChange}
    />
  );
}

export default Editor;
