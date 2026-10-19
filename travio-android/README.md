# Travio — aplikacja na Androida (koncept B: Map-First Explorer)

Natywna aplikacja Android (Kotlin + Jetpack Compose + Material 3), zbudowana od zera na
podstawie wybranego kierunku wizualnego "B. Map-First Explorer": mapa jako ekran startowy,
jasny i ciemny motyw z przełącznikiem w aplikacji (Więcej → Wygląd aplikacji), plan dnia,
budżet z licznikiem wydatków i ciekawostki o mieście.

## Build

Projekt jest zweryfikowany: po otwarciu dostępu sieciowego do `dl.google.com` (domyślnie
zablokowanego w tym środowisku) zbudował się tu od zera poleceniem `./gradlew assembleDebug`
i dał działający plik `app-debug.apk`. Po drodze wyszły dwa realne błędy w kodzie (zły typ
odbiorcy lambdy w `HomeMapScreen.kt` i eksperymentalne API w `BudgetScreen.kt`) — oba naprawione,
więc kod w tym repozytorium jest już poprawiony.

## Jak zbudować plik .apk (2 minuty)

**Opcja A — Android Studio (najprostsza):**
1. Otwórz Android Studio → "Open" → wskaż ten folder (`travio-android`).
2. Poczekaj, aż Gradle pobierze zależności (pasek postępu u dołu).
3. Kliknij zielony trójkąt "Run" (albo Build → Build Bundle(s) / APK(s) → Build APK(s)).
4. Gotowy plik znajdziesz w `app/build/outputs/apk/debug/app-debug.apk`.

**Opcja B — terminal:**
```bash
cd travio-android
./gradlew assembleDebug
# plik: app/build/outputs/apk/debug/app-debug.apk
```
(Wymaga tylko zainstalowanej Javy 17+ — resztę, łącznie z Gradle, pobierze sam wrapper.)

Zainstaluj plik `.apk` na telefonie (albo `adb install app-debug.apk`), żeby go uruchomić.

## Co jest w środku

- **Start (mapa)** — pełnoekranowa mapa dnia (OpenStreetMap przez OSMDroid, bez klucza API) z
  numerowanymi znacznikami i realną trasą pieszą między punktami (OSRM, też bez klucza — ten
  sam darmowy serwis co w wersji HTML). Pogoda na żywo (Open-Meteo) i licznik wydanego budżetu
  jako pływające kafelki, karta dnia u dołu z podglądem najbliższych punktów.
- **Plan** — pełny plan dnia z przełącznikiem dni, opisem każdego miejsca i orientacyjnym
  kosztem tam, gdzie jest znany.
- **Budżet** — suma wydana vs. zaplanowana, podział na kategorie, lista ostatnich wydatków i
  przycisk "+" do dodania własnego wydatku (zapisywany w pamięci aplikacji).
- **Więcej** — przełącznik motywu (System / Jasny / Ciemny), dane hotelu i ciekawostki o
  Barcelonie.
- Dane wycieczki (prawdziwy plan "Barcelona 2" z 7 dniami i 41 punktami) są dołączone jako
  plik `app/src/main/assets/trip_barcelona2.json` — można go łatwo podmienić na inną wycieczkę
  bez zmian w kodzie, trzymając się tego samego schematu.

## Architektura

- **UI**: Jetpack Compose + Material 3, jeden `MainActivity` z dolną nawigacją (`NavigationBar`)
  i 4 ekranami w `ui/screens/`.
- **Motyw**: `ui/theme/` — osobne palety jasna/ciemna (`Color.kt`), wybór zapisywany trwale przez
  Jetpack DataStore (`data/ThemePreferenceStore.kt`).
- **Dane**: `data/TripModels.kt` (modele), `data/TripRepository.kt` (wczytuje JSON z assets).
  Wydatki w `ExpenseStore` (w pamięci — do podmiany na Room/DataStore przy dalszym rozwoju).
- **Sieć**: `network/` — Retrofit + Gson, dwa darmowe, bezkluczowe API: Open-Meteo (pogoda) i
  OSRM (trasa piesza). Mapa: OSMDroid (kafelki OpenStreetMap, też bez klucza).
- **Stan**: jeden `TripViewModel` (`ui/TripViewModel.kt`) z `StateFlow`, współdzielony przez
  wszystkie ekrany.

## Dalszy rozwój (pomysły, nie zrobione w tej wersji)

- Zapisywanie wydatków trwale (Room albo DataStore zamiast pamięci).
- Import własnej wycieczki z pliku JSON zamiast jednej, wbudowanej.
- Ekrany "Podróż" (loty/transport) i "Extra" (dodatkowe miejsca) — pominięte w tej wersji, żeby
  dowieźć solidny rdzeń (mapa, plan, budżet, motywy) zamiast rozciągać się na wszystko naraz.
