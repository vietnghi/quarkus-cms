import React, { useState, useEffect } from "react";
import { useList } from "@refinedev/core";

/** Simple media library browser page — upload + list */
export const MediaPage: React.FC = () => {
  const [files, setFiles] = useState<any[]>([]);
  const [uploading, setUploading] = useState(false);

  const loadFiles = () => {
    fetch("/cms-admin/api/relations")
      .then(r => r.json())
      .catch(() => []);
  };

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    const formData = new FormData();
    formData.append("file", file);
    await fetch("/cms-admin/api/media", { method: "POST", body: formData });
    setUploading(false);
    loadFiles();
  };

  useEffect(() => { loadFiles(); }, []);

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>Media Library</h2>
      <div style={{ marginBottom: 16, padding: 16, background: "#e8f0fe", borderRadius: 8 }}>
        <input type="file" onChange={handleUpload} disabled={uploading} />
        {uploading && <span> Uploading...</span>}
      </div>
      <p>Uploaded files appear here. Use /cms-admin/api/media to manage.</p>
    </div>
  );
};
