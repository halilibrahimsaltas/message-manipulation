import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';

function MessageDetail() {
  const { id } = useParams();
  const [message, setMessage] = useState(null);

  useEffect(() => {
    fetch(`/api/messages/${id}`)
      .then(res => res.json())
      .then(data => setMessage(data))
      .catch(err => console.error('Detay çekilemedi:', err));
  }, [id]);

  if (!message) return <div>Yükleniyor...</div>;

  return (
    <div>
      <h2>Mesaj Detayı</h2>
      <p>ID: {message.id}</p>
      <p>Gönderen: {message.sender}</p>
      <p>İçerik: {message.content}</p>
      <p>Oluşturulma: {message.createdAt}</p>
      <p>Dönüşmüş Link: {message.convertedContent}</p>
      <p>Telegram: {message.forwardedToTelegram ? 'Evet' : 'Hayır'}</p>
      <p>Slack: {message.forwardedToSlack ? 'Evet' : 'Hayır'}</p>
    </div>
  );
}

export default MessageDetail;