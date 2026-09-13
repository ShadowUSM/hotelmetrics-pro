# hotelmetrics-pro

Ten sam serwis GitHub Pages hostuje kilka niezależnych, jednoplikowych aplikacji webowych. Każda żyje w swoim katalogu i nie wpływa na pozostałe.

## Aplikacje

- **/** (`index.html`) — HotelMetrics Pro, F&B Revenue Analytics dla hoteli.
- **/travio/** — Travio, uniwersalna aplikacja do planowania wycieczek (dowolne miasto, wiele wycieczek, offline).

## Uruchomienie GitHub Pages

1. Wejdź w ustawienia repozytorium: **Settings → Pages**.
2. W sekcji "Build and deployment" wybierz **Source: Deploy from a branch**.
3. Jako branch wybierz `main` i folder **/ (root)**, zapisz.
4. Po chwili strona będzie dostępna pod `https://<twoj-login>.github.io/hotelmetrics-pro/`, a Travio pod `https://<twoj-login>.github.io/hotelmetrics-pro/travio/`.
5. Zmiany trafiają na GitHub Pages dopiero po zmergowaniu ich do brancha `main` — jeśli pracujesz na innym branchu (np. z Claude), zmerguj go do `main`, żeby wdrożyć nową wersję.

Travio nie wymaga żadnego backendu ani builda — to statyczny plik HTML + manifest + service worker, więc powyższa konfiguracja jest kompletna.

## Instalacja Travio na telefonie

Po wejściu na `https://<twoj-login>.github.io/hotelmetrics-pro/travio/`:

- **Android (Chrome):** menu (⋮) → **"Zainstaluj aplikację"** / **"Dodaj do ekranu głównego"**. Ikona pojawi się jak natywna aplikacja, a dzięki service workerowi aplikacja działa też bez internetu (np. w podróży bez zasięgu) — po pierwszym wejściu online wszystko jest zapisane w cache telefonu.
- **iPhone/iPad (Safari):** przycisk udostępniania (□↑) → **"Dodaj do ekranu początkowego"**. iOS nie wspiera automatycznych podpowiedzi instalacji, ale ikona i pełnoekranowy tryb działają identycznie.
- Dane wycieczek (postęp, notatki, zdjęcia z dziennika) zapisują się lokalnie na urządzeniu (localStorage) — nie synchronizują się jeszcze między urządzeniami/kontami.

## Plany rozwoju Travio

Kolejny etap: synchronizacja danych przez Firebase (Firestore + Auth), żeby:
- założyć konto i zalogować się na kilku urządzeniach z tymi samymi wycieczkami,
- zaprosić inne osoby do wspólnej wycieczki, żeby widziały ten sam plan i wspólnie go edytowały.

Ponieważ Travio to statyczna aplikacja hostowana na GitHub Pages, doda się to czysto po stronie klienta (Firebase JS SDK) — bez zmiany sposobu hostowania.
