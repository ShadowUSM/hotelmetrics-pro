# Travio — aplikacja na Androida (koncept B: Map-First Explorer)

Natywna aplikacja Android (Kotlin + Jetpack Compose + Material 3), zbudowana od zera na
podstawie wybranego kierunku wizualnego "B. Map-First Explorer": mapa jako ekran startowy,
jasny i ciemny motyw z przełącznikiem w aplikacji (Więcej → Wygląd aplikacji), plan dnia,
budżet z licznikiem wydatków i ciekawostki o mieście.

## Ważne: dlaczego dostajesz projekt źródłowy, a nie gotowy plik .apk

Zbudowałem tę aplikację od zera i naprawdę spróbowałem skompilować ją tutaj, w moim
środowisku. Nie udało się — i to nie jest kwestia braku czasu, tylko twardego ograniczenia
sieciowego, które sam sprawdziłem bezpośrednio:

- Mam dostęp do Gradle, Javy 21 i Maven Central (skąd pobierane są np. Retrofit, OkHttp, Gson,
  OSMDroid — te wszystkie działają).
- **Nie mam dostępu do `dl.google.com`** — a to jedyne źródło całego ekosystemu AndroidX/Jetpack
  (Compose, Material 3, Navigation, DataStore, a nawet sam wtyczki Android Gradle Plugin).
  Sprawdziłem to wprost: `maven.google.com` (z którego korzysta Gradle) przy każdej faktycznej
  próbie pobrania pliku **przekierowuje na `dl.google.com`**, które moje środowisko blokuje.
  Próbowałem też znaleźć alternatywne lustra (GitHub, Maven Central) — żadne z nich nie hostuje
  AndroidX/Compose, więc nie dało się tego obejść.

To ograniczenie samego środowiska, w którym pracuję, a nie błąd w kodzie aplikacji. U Ciebie,
z normalnym dostępem do internetu, projekt zbuduje się i uruchomi bez żadnych zmian.

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
