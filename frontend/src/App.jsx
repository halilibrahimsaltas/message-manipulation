import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import './App.css';
import MessagesList from './pages/MessagesList';
import MessageSearch from './pages/MessageSearch';
import SettingsPage from './pages/SettingsPage';

function App() {
  return (
    <Router>
      <div className="App">
        <nav>
          <div className="nav-content">
            <h1 className="nav-title">WhatsApp Dashboard</h1>
            <ul>
              <li><Link to="/">Ana Sayfa</Link></li>
              <li><Link to="/search">Mesaj Ara</Link></li>
              <li><Link to="/settings">Ayarlar</Link></li>
            </ul>
          </div>
        </nav>

        <div className="content-container">
          <Routes>
            <Route path="/" element={<MessagesList />} />
            <Route path="/search" element={<MessageSearch />} />
            <Route path="/settings" element={<SettingsPage />} />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;