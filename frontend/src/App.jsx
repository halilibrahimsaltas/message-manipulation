import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import './App.css';
import MessagesList from './pages/MessagesList';
import MessageSearch from './pages/MessageSearch';

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
            </ul>
          </div>
        </nav>

        <div className="content-container">
          <Routes>
            <Route path="/" element={<MessagesList />} />
            <Route path="/search" element={<MessageSearch />} />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;