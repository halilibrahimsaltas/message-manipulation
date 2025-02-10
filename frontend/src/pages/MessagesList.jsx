import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

function MessagesList() {
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    fetch('/api/messages')
      .then((res) => {
        if (!res.ok) throw new Error('Sunucu hatası');
        return res.json();
      })
      .then((data) => {
        setMessages(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Mesajları çekmede hata:', err);
        setError(err.message);
        setLoading(false);
      });
  }, []);

  const formatDate = (dateString) => {
    try {
      if (!dateString) return 'Tarih yok';
      
      // PostgreSQL timestamp string'ini parçalara ayır
      const [datePart, timePart] = dateString.split(' ');
      const [year, month, day] = datePart.split('-');
      const [hour, minute, second] = timePart.split('.')[0].split(':');
      
      // Türkçe ay isimleri
      const months = [
        'Ocak', 'Şubat', 'Mart', 'Nisan', 'Mayıs', 'Haziran',
        'Temmuz', 'Ağustos', 'Eylül', 'Ekim', 'Kasım', 'Aralık'
      ];

      return `${day} ${months[parseInt(month) - 1]} ${year} ${hour}:${minute}:${second}`;
      
    } catch (e) {
      console.error('Tarih formatı hatası:', e);
      return 'Tarih hatası';
    }
  };

  if (loading) return <div className="loading">Yükleniyor...</div>;
  if (error) return <div className="error">Hata: {error}</div>;

  return (
    <div>
      <h2 className="page-title">Mesaj Listesi</h2>
      {messages.length === 0 ? (
        <p>Henüz mesaj bulunmuyor.</p>
      ) : (
        <table className="messages-table">
          <thead>
            <tr>
              <th>Gönderen</th>
              <th>Mesaj</th>
              <th>Tarih</th>
            </tr>
          </thead>
          <tbody>
            {messages.map(msg => (
              <tr key={msg.id}>
                <td><strong>{msg.sender}</strong></td>
                <td>{msg.content}</td>
                <td>{formatDate(msg.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default MessagesList; 