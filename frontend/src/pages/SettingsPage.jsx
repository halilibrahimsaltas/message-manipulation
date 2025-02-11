import React, { useState, useEffect } from 'react';

function SettingsPage() {
  const [settings, setSettings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState('');
  const [editedSettings, setEditedSettings] = useState({});

  useEffect(() => {
    fetchSettings();
  }, []);

  const fetchSettings = async () => {
    try {
      setLoading(true);
      const response = await fetch('/api/settings');
      if (!response.ok) throw new Error('Ayarlar yüklenemedi');
      const data = await response.json();
      setSettings(data);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (propKey, value) => {
    setEditedSettings(prev => ({
      ...prev,
      [propKey]: value
    }));
  };

  const saveAllChanges = async () => {
    try {
      const promises = Object.entries(editedSettings).map(([propKey, value]) =>
        fetch(`/api/settings/${propKey}`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ value })
        })
      );

      await Promise.all(promises);
      setSuccessMessage('Tüm ayarlar başarıyla güncellendi!');
      setTimeout(() => setSuccessMessage(''), 3000);
      setEditedSettings({});
      fetchSettings();
    } catch (err) {
      setError('Ayarlar güncellenirken hata oluştu');
    }
  };

  if (loading) return <div className="loading">Yükleniyor...</div>;
  if (error) return <div className="error">Hata: {error}</div>;

  const hasChanges = Object.keys(editedSettings).length > 0;

  return (
    <div>
      <h2 className="page-title">Sistem Ayarları</h2>
      <div className="settings-container">
        {successMessage && (
          <div className="success-message">{successMessage}</div>
        )}
        <div className="settings-grid">
          {settings.map(setting => (
            <div key={setting.id} className="setting-item">
              <div className="setting-header">
                <label>{setting.propKey}</label>
                <small className="setting-description">
                  {getSettingDescription(setting.propKey)}
                </small>
              </div>
              <div className="setting-input">
                <input
                  type="text"
                  defaultValue={setting.propValue}
                  onChange={(e) => handleInputChange(setting.propKey, e.target.value)}
                  placeholder={`${setting.propKey} değerini girin`}
                />
              </div>
            </div>
          ))}
        </div>
        {hasChanges && (
          <div className="settings-actions">
            <button 
              className="save-button"
              onClick={saveAllChanges}
            >
              Değişiklikleri Kaydet
            </button>
            <button 
              className="cancel-button"
              onClick={() => {
                setEditedSettings({});
                fetchSettings();
              }}
            >
              İptal
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

function getSettingDescription(key) {
  const descriptions = {
    'telegram.bot.token': 'Telegram Bot API anahtarı',
    'telegram.bot.chatIds': 'Mesajların iletileceği Telegram kanal/grup ID\'leri (virgülle ayrılmış)',
    'link.conversion.ref': 'Link dönüştürme referans parametresi'
  };
  return descriptions[key] || 'Ayar açıklaması';
}

export default SettingsPage;
