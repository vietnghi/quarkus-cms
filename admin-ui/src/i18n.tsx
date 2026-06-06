import React, { useState } from "react";

const LOCALES = ["en", "fr", "de", "es", "vi"];

export const LocaleSwitch: React.FC = () => {
  const [locale, setLocale] = useState(() => localStorage.getItem("cms-locale") || "en");

  const changeLocale = (l: string) => {
    setLocale(l);
    localStorage.setItem("cms-locale", l);
    window.location.reload();
  };

  return (
    <select value={locale} onChange={e => changeLocale(e.target.value)}
      style={{ padding: "4px 8px", border: "1px solid #555", borderRadius: 4, background: "#333", color: "white", marginRight: 8 }}>
      {LOCALES.map(l => <option key={l} value={l}>{l.toUpperCase()}</option>)}
    </select>
  );
};
