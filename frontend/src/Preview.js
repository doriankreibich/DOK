import React from 'react';
import ReactMarkdown from 'react-markdown';

function Preview({ content }) {
  return (
    <div id="preview-pane" className="preview-pane">
      <ReactMarkdown>{content}</ReactMarkdown>
    </div>
  );
}

export default Preview;
