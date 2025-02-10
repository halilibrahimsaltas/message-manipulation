import React, { useState } from 'react';
import { Link } from 'react-router-dom';

function MessageSearch() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [searching, setSearching] = useState(false);

  function handleSearch() {
    if (!query.trim()) return;
    
    setSearching(true);
    fetch(`/api/messages/search?q=${encodeURIComponent(query)}`)
      .then(res => res.json())
      .then(data => {
        setResults(data);
        setSearching(false);
      })
      .catch(err => {
        console.error('Arama hatası:', err);
        setSearching(false);
      });
  }

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

  return (
    <div>
      <h2 className="page-title">Mesaj Arama</h2>
      <div className="search-container">
        <input 
          value={query} 
          onChange={(e) => setQuery(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
          placeholder="Aramak istediğiniz kelime"
        />
        <button onClick={handleSearch} disabled={searching}>
          {searching ? 'Aranıyor...' : 'Ara'}
        </button>
      </div>

      <div className="search-results">
        {results.length > 0 && (
          <table className="messages-table">
            <thead>
              <tr>
                <th>Gönderen</th>
                <th>Mesaj</th>
                <th>Tarih</th>
              </tr>
            </thead>
            <tbody>
              {results.map(msg => (
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
    </div>
  );
}

export default MessageSearch;