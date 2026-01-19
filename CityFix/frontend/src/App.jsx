import React, { useMemo, useState } from "react";
import { Link, Route, Routes, useLocation, useNavigate } from "react-router-dom";
import {
  MapContainer,
  Marker,
  Popup,
  TileLayer,
  useMap,
  useMapEvents,
} from "react-leaflet";
import L from "leaflet";
import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080/api";

const apiRequest = async (path, options = {}) => {
  const url = `${API_BASE}${path}`;
  const config = {
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
    ...options,
  };

  if (config.body && typeof config.body !== "string") {
    config.body = JSON.stringify(config.body);
  }

  const response = await fetch(url, config);
  const contentType = response.headers.get("content-type") || "";
  const isJson = contentType.includes("application/json");
  const payload = response.status === 204 ? null : isJson ? await response.json() : await response.text();

  if (!response.ok) {
    const error = new Error(`HTTP ${response.status}`);
    error.payload = payload;
    throw error;
  }

  return payload;
};

const Header = ({ onLogout, isLoggedIn }) => {
  const location = useLocation();
  return (
    <header className="app-header">
      <div className="brand">
        <span className="brand-mark">🏙️</span>
        <div>
          <h1>CityFix</h1>
          <p>Panel mieszkańca</p>
        </div>
      </div>
      <nav className="nav-links">
        <Link to="/" className={location.pathname === "/" ? "active" : ""}>
          Panel
        </Link>
        <Link to="/login" className={location.pathname === "/login" ? "active" : ""}>
          Logowanie
        </Link>
        <Link to="/register" className={location.pathname === "/register" ? "active" : ""}>
          Rejestracja
        </Link>
        {isLoggedIn ? (
          <button type="button" className="ghost" onClick={onLogout}>
            Wyloguj
          </button>
        ) : null}
      </nav>
    </header>
  );
};

const FitBounds = ({ points }) => {
  const map = useMap();
  useMemo(() => {
    if (!points.length) {
      return;
    }
    map.fitBounds(points, { padding: [40, 40] });
  }, [points, map]);
  return null;
};

const ReportsMap = ({ reports }) => {
  const points = useMemo(
    () =>
      reports
        .filter((report) => typeof report.latitude === "number" && typeof report.longitude === "number")
        .map((report) => [report.latitude, report.longitude]),
    [reports],
  );

  return (
    <MapContainer center={[52.2297, 21.0122]} zoom={6} className="map map-large">
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      {reports.map((report) => {
        if (typeof report.latitude !== "number" || typeof report.longitude !== "number") {
          return null;
        }
        const title = report.title || "Zgłoszenie";
        const meta = [report.status ? `Status: ${report.status}` : null, report.category ? `Kategoria: ${report.category}` : null]
          .filter(Boolean)
          .join(" · ");
        return (
          <Marker key={report.id} position={[report.latitude, report.longitude]}>
            <Popup>
              <strong>{title}</strong>
              <br />
              {meta || "Brak szczegółów"}
            </Popup>
          </Marker>
        );
      })}
      <FitBounds points={points} />
    </MapContainer>
  );
};

const CreateMapPicker = ({ value, onChange }) => {
  const position = value.latitude && value.longitude ? [Number(value.latitude), Number(value.longitude)] : [52.2297, 21.0122];

  const MapEvents = () => {
    useMapEvents({
      click(event) {
        onChange({
          ...value,
          latitude: event.latlng.lat.toFixed(6),
          longitude: event.latlng.lng.toFixed(6),
        });
      },
    });
    return null;
  };

  return (
    <MapContainer center={position} zoom={12} className="map">
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <MapEvents />
      {value.latitude && value.longitude ? (
        <Marker position={position} />
      ) : null}
    </MapContainer>
  );
};

const Home = ({ currentUser, onRefreshUser }) => {
  const [reports, setReports] = useState([]);
  const [createForm, setCreateForm] = useState({
    title: "",
    description: "",
    category: "",
    priority: "",
    latitude: "",
    longitude: "",
  });
  const [updateForm, setUpdateForm] = useState({
    id: "",
    title: "",
    description: "",
    status: "",
    category: "",
    priority: "",
    latitude: "",
    longitude: "",
  });
  const [isEditing, setIsEditing] = useState(false);
  const [deleteId, setDeleteId] = useState("");
  const updateFormRef = React.useRef(null);

  const loadReports = async () => {
    try {
      const result = await apiRequest("/reports");
      setReports(result || []);
    } catch (error) {
    }
  };

  const onCreateSubmit = async (event) => {
    event.preventDefault();
    try {
      const payload = {
        title: createForm.title.trim(),
        description: createForm.description.trim() || null,
        category: createForm.category.trim() || null,
        priority: createForm.priority.trim() || null,
        latitude: createForm.latitude ? Number(createForm.latitude) : null,
        longitude: createForm.longitude ? Number(createForm.longitude) : null,
      };
      await apiRequest("/reports", { method: "POST", body: payload });
      await loadReports();
    } catch (error) {
    }
  };

  const onUpdateSubmit = async (event) => {
    event.preventDefault();
    try {
      const payload = {
        title: updateForm.title.trim() || null,
        description: updateForm.description.trim() || null,
        status: updateForm.status.trim() || null,
        category: updateForm.category.trim() || null,
        priority: updateForm.priority.trim() || null,
        latitude: updateForm.latitude ? Number(updateForm.latitude) : null,
        longitude: updateForm.longitude ? Number(updateForm.longitude) : null,
      };
      await apiRequest(`/reports/${updateForm.id}`, { method: "PUT", body: payload });
      await loadReports();
    } catch (error) {
    }
  };

  const onDeleteSubmit = async (event) => {
    event.preventDefault();
    try {
      await apiRequest(`/reports/${deleteId}`, { method: "DELETE" });
      await loadReports();
    } catch (error) {
    }
  };

  React.useEffect(() => {
    onRefreshUser();
    loadReports();
  }, []);

  return (
    <main>
      <section className="grid grid-2">
        <article className="card">
          <h2>Zgłoszenia</h2>
          <div className="actions">
            <button type="button" onClick={loadReports}>
              Odśwież listę
            </button>
          </div>
          <div className="list">
            {reports.length === 0 ? (
              <div className="note">Brak zgłoszeń do wyświetlenia.</div>
            ) : (
              reports.map((report) => (
                <div className="list-item" key={report.id}>
                  <h4>
                    #{report.id} {report.title || "(bez tytułu)"}
                  </h4>
                  <p>
                    {[report.status ? `Status: ${report.status}` : null, report.category ? `Kategoria: ${report.category}` : null, report.priority ? `Priorytet: ${report.priority}` : null]
                      .filter(Boolean)
                      .join(" · ")}
                  </p>
                  <p>Utworzono: {report.createdAt || "-"}</p>
                  {currentUser && currentUser.id === report.userId ? (
                    <div className="actions">
                      <button
                        type="button"
                        onClick={() => {
                          setUpdateForm({
                            id: report.id,
                            title: report.title || "",
                            description: report.description || "",
                            status: report.status || "",
                            category: report.category || "",
                            priority: report.priority || "",
                            latitude:
                              report.latitude === null || report.latitude === undefined ? "" : String(report.latitude),
                            longitude:
                              report.longitude === null || report.longitude === undefined ? "" : String(report.longitude),
                          });
                          setIsEditing(true);
                          updateFormRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
                        }}
                      >
                        Edytuj
                      </button>
                    </div>
                  ) : null}
                </div>
              ))
            )}
          </div>
        </article>
      </section>

      <section className="card map-card">
        <div className="card-header">
          <h2>Mapa zgłoszeń</h2>
          <div className="note">
            Dane mapy © <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>
          </div>
        </div>
        <ReportsMap reports={reports} />
      </section>

      <section className="grid grid-2">
        <article className="card">
          <h2>Nowe zgłoszenie</h2>
          {!currentUser ? (
            <div className="note">Zaloguj się, aby dodać nowe zgłoszenie.</div>
          ) : (
            <form className="form" onSubmit={onCreateSubmit}>
              <label>
                Tytuł
                <input
                  value={createForm.title}
                  onChange={(event) => setCreateForm({ ...createForm, title: event.target.value })}
                  maxLength={255}
                  required
                />
              </label>
              <label>
                Lokalizacja (kliknij na mapie)
                <CreateMapPicker value={createForm} onChange={(next) => setCreateForm(next)} />
              </label>
              <div className="row">
                <label>
                  Latitude
                  <input
                    value={createForm.latitude}
                    onChange={(event) => setCreateForm({ ...createForm, latitude: event.target.value })}
                  />
                </label>
                <label>
                  Longitude
                  <input
                    value={createForm.longitude}
                    onChange={(event) => setCreateForm({ ...createForm, longitude: event.target.value })}
                  />
                </label>
              </div>
              <label>
                Opis
                <textarea
                  rows={4}
                  value={createForm.description}
                  onChange={(event) => setCreateForm({ ...createForm, description: event.target.value })}
                />
              </label>
              <label>
                Kategoria
                <select
                  value={createForm.category}
                  onChange={(event) => setCreateForm({ ...createForm, category: event.target.value })}
                >
                  <option value="">Wybierz kategorię</option>
                  <option value="ROAD_DAMAGE">ROAD_DAMAGE</option>
                  <option value="LIGHTING">LIGHTING</option>
                  <option value="GRAFFITI">GRAFFITI</option>
                </select>
              </label>
              <label>
                Priorytet
                <select
                  value={createForm.priority}
                  onChange={(event) => setCreateForm({ ...createForm, priority: event.target.value })}
                >
                  <option value="">Wybierz priorytet</option>
                  <option value="LOW">LOW</option>
                  <option value="MEDIUM">MEDIUM</option>
                  <option value="HIGH">HIGH</option>
                </select>
              </label>
              <button type="submit">Wyślij zgłoszenie</button>
            </form>
          )}
        </article>

        <article className="card">
          <h2>Aktualizuj zgłoszenie</h2>
          {!currentUser ? (
            <div className="note">Zaloguj się, aby edytować lub usuwać zgłoszenia.</div>
          ) : !isEditing ? (
            <div className="note">Kliknij „Edytuj” przy wybranym zgłoszeniu.</div>
          ) : (
            <>
              <form className="form" onSubmit={onUpdateSubmit} ref={updateFormRef}>
                <label>
                  ID zgłoszenia
                  <input
                    value={updateForm.id}
                    onChange={(event) => setUpdateForm({ ...updateForm, id: event.target.value })}
                    required
                  />
                </label>
                <label>
                  Tytuł
                  <input
                    value={updateForm.title}
                    onChange={(event) => setUpdateForm({ ...updateForm, title: event.target.value })}
                  />
                </label>
                <label>
                  Opis
                  <textarea
                    rows={4}
                    value={updateForm.description}
                    onChange={(event) => setUpdateForm({ ...updateForm, description: event.target.value })}
                  />
                </label>
                <label>
                  Status
                  <select
                    value={updateForm.status}
                    onChange={(event) => setUpdateForm({ ...updateForm, status: event.target.value })}
                  >
                    <option value="">Wybierz status</option>
                    <option value="OPEN">OPEN</option>
                    <option value="IN_PROGRESS">IN_PROGRESS</option>
                    <option value="RESOLVED">RESOLVED</option>
                    <option value="REJECTED">REJECTED</option>
                  </select>
                </label>
                <label>
                  Kategoria
                  <input
                    value={updateForm.category}
                    onChange={(event) => setUpdateForm({ ...updateForm, category: event.target.value })}
                  />
                </label>
                <label>
                  Priorytet
                  <input
                    value={updateForm.priority}
                    onChange={(event) => setUpdateForm({ ...updateForm, priority: event.target.value })}
                  />
                </label>
                <div className="row">
                  <label>
                    Latitude
                    <input
                      value={updateForm.latitude}
                      onChange={(event) => setUpdateForm({ ...updateForm, latitude: event.target.value })}
                    />
                  </label>
                  <label>
                    Longitude
                    <input
                      value={updateForm.longitude}
                      onChange={(event) => setUpdateForm({ ...updateForm, longitude: event.target.value })}
                    />
                  </label>
                </div>
                <div className="actions">
                  <button type="submit">Zapisz</button>
                  <button
                    type="button"
                    className="ghost"
                    onClick={() => {
                      setIsEditing(false);
                      setUpdateForm({
                        id: "",
                        title: "",
                        description: "",
                        status: "",
                        category: "",
                        priority: "",
                        latitude: "",
                        longitude: "",
                      });
                    }}
                  >
                    Anuluj
                  </button>
                </div>
              </form>
              <form className="form" onSubmit={onDeleteSubmit}>
                <h3>Usuń zgłoszenie</h3>
                <label>
                  ID zgłoszenia
                  <input
                    value={deleteId}
                    onChange={(event) => setDeleteId(event.target.value)}
                    required
                  />
                </label>
                <button type="submit" className="danger">
                  Usuń
                </button>
              </form>
            </>
          )}
        </article>
      </section>
    </main>
  );
};

const Login = () => {
  const [form, setForm] = useState({ username: "", password: "" });
  const navigate = useNavigate();

  const onSubmit = async (event) => {
    event.preventDefault();
    try {
      await apiRequest("/users/login", { method: "POST", body: form });
      navigate("/");
    } catch (error) {
    }
  };

  return (
    <main>
      <section className="grid grid-2">
        <article className="card">
          <h2>Logowanie</h2>
          <form className="form" onSubmit={onSubmit}>
            <label>
              Login
              <input
                value={form.username}
                onChange={(event) => setForm({ ...form, username: event.target.value })}
                required
              />
            </label>
            <label>
              Hasło
              <input
                type="password"
                value={form.password}
                onChange={(event) => setForm({ ...form, password: event.target.value })}
                required
              />
            </label>
            <button type="submit">Zaloguj</button>
          </form>
        </article>
        <article className="card">
          <h2>Nie masz konta?</h2>
          <div className="note">
            Przejdź do rejestracji, aby założyć konto i tworzyć zgłoszenia.
          </div>
          <Link className="btn-link" to="/register">
            Rejestracja
          </Link>
        </article>
      </section>
    </main>
  );
};

const Register = () => {
  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
    firstName: "",
    lastName: "",
    phone: "",
  });
  const navigate = useNavigate();

  const onSubmit = async (event) => {
    event.preventDefault();
    try {
      await apiRequest("/users/register", { method: "POST", body: form });
      navigate("/login");
    } catch (error) {
    }
  };

  return (
    <main>
      <section className="grid grid-2">
        <article className="card">
          <h2>Rejestracja</h2>
          <form className="form" onSubmit={onSubmit}>
            <label>
              Login
              <input
                value={form.username}
                onChange={(event) => setForm({ ...form, username: event.target.value })}
                required
              />
            </label>
            <label>
              Email
              <input
                type="email"
                value={form.email}
                onChange={(event) => setForm({ ...form, email: event.target.value })}
                required
              />
            </label>
            <label>
              Hasło
              <input
                type="password"
                value={form.password}
                onChange={(event) => setForm({ ...form, password: event.target.value })}
                required
                minLength={8}
              />
            </label>
            <label>
              Imię
              <input
                value={form.firstName}
                onChange={(event) => setForm({ ...form, firstName: event.target.value })}
              />
            </label>
            <label>
              Nazwisko
              <input
                value={form.lastName}
                onChange={(event) => setForm({ ...form, lastName: event.target.value })}
              />
            </label>
            <label>
              Telefon
              <input
                value={form.phone}
                onChange={(event) => setForm({ ...form, phone: event.target.value })}
              />
            </label>
            <button type="submit">Załóż konto</button>
          </form>
        </article>
        <article className="card">
          <h2>Masz już konto?</h2>
          <div className="note">Zaloguj się, aby przejść do panelu zgłoszeń.</div>
          <Link className="btn-link" to="/login">
            Logowanie
          </Link>
        </article>
      </section>
    </main>
  );
};

const App = () => {
  const [currentUser, setCurrentUser] = useState(null);
  const navigate = useNavigate();

  const refreshUser = async () => {
    try {
      const user = await apiRequest("/users/me");
      setCurrentUser(user);
    } catch (error) {
      setCurrentUser(null);
    }
  };

  const handleLogout = async () => {
    try {
      await apiRequest("/users/logout", { method: "POST" });
      setCurrentUser(null);
      navigate("/login");
    } catch (error) {
    }
  };

  return (
    <div className="app-shell">
      <Header onLogout={handleLogout} isLoggedIn={Boolean(currentUser)} />
      <Routes>
        <Route path="/" element={<Home currentUser={currentUser} onRefreshUser={refreshUser} />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
      </Routes>
    </div>
  );
};

export default App;
