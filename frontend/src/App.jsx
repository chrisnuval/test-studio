// frontend/src/App.js
import React, { useState } from 'react';
import './App.css';

function App() {
  const [recording, setRecording] = useState(false);
  const [url, setUrl] = useState('https://practicesoftwaretesting.com');
  const [cleanedFeature, setCleanedFeature] = useState('');
  const [locators, setLocators] = useState('');

  const startRecording = async () => {
    setCleanedFeature('');
    setLocators('');
    if (!url) return;
    await fetch('/record/start', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ url }),
    });
    setRecording(true);
  };

  const stopRecording = async () => {
    await fetch('/record/stop', { method: 'POST' });
    setRecording(false);
  };

  const playback = async () => {
    await fetch('/record/playback', { method: 'POST' });
  };

  const generateFeature = async () => {
    setCleanedFeature('');
    setLocators('');
    const response = await fetch('/record/feature', { method: 'POST' });
    const rawText = await response.text();

    const cleanResponse = await fetch('/record/clean-feature?provider=ollama&model=codellama', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ rawFeature: rawText }),
    });
    const cleanedText = await cleanResponse.text();
    setCleanedFeature(cleanedText);

    try {
      const locatorRes = await fetch('/record/locators');
      if (!locatorRes.ok) throw new Error(`Status ${locatorRes.status}`);
      const json = await locatorRes.json();
      const uniqueLocators = [];
      const seen = new Set();
      for (const entry of json) {
        const key = `${entry.name}-${entry.lookupDetails.findBy}-${entry.lookupDetails.value}`;
        if (!seen.has(key)) {
          seen.add(key);
          uniqueLocators.push(entry);
        }
      }
      setLocators(JSON.stringify(uniqueLocators, null, 2));
    } catch (e) {
      console.error('Failed to load locators.json:', e);
      setLocators('// Failed to load locators');
    }
  };

  const exportLocators = () => {
    const blob = new Blob([locators], { type: 'application/json' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'locators.json';
    a.click();
  };

  const exportFeature = () => {
    const blob = new Blob([cleanedFeature], { type: 'text/plain' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'feature.feature';
    a.click();
  };

  return (
      <div style={{ padding: '2rem', maxWidth: '900px', margin: '0 auto', fontFamily: 'Segoe UI, sans-serif' }}>
        <h1>Test Studio Recorder</h1>
        <label>
          URL to record/playback:
          <input
              type="text"
              value={url}
              onChange={e => setUrl(e.target.value)}
              style={{ marginLeft: '1rem', width: '400px' }}
              disabled={recording}
          />
        </label>
        <div style={{ marginTop: '1rem' }}>
          <button onClick={recording ? stopRecording : startRecording}>
            {recording ? 'Stop Recording' : 'Start Recording'}
          </button>
          <button onClick={playback} style={{ marginLeft: '1rem' }}>
            Playback
          </button>
          <button onClick={generateFeature} style={{ marginLeft: '1rem' }}>
            Generate Feature File
          </button>
        </div>

        {cleanedFeature && (
            <div style={{ marginTop: '2rem' }}>
              <h2>Cleaned Feature File</h2>
              <textarea
                  value={cleanedFeature}
                  readOnly
                  rows={14}
                  style={{ width: '100%', fontFamily: 'monospace', fontSize: '14px' }}
              />
              <button onClick={exportFeature} style={{ marginTop: '1rem' }}>Download Feature File</button>
            </div>
        )}

        {locators && (
            <div style={{ marginTop: '2rem' }}>
              <h2>Generated Locators</h2>
              <textarea
                  value={locators}
                  readOnly
                  rows={12}
                  style={{ width: '100%', fontFamily: 'monospace', fontSize: '14px' }}
              />
              <button onClick={exportLocators} style={{ marginTop: '1rem' }}>Download locators.json</button>
            </div>
        )}
      </div>
  );
}

export default App;
