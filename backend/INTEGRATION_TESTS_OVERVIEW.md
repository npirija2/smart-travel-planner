# Integracijski Testovi

## Šta je cilj

U repozitoriju su već postojali:

- unit testovi za servise
- `@WebMvcTest` testovi za kontrolere
- osnovni `contextLoads()` smoke testovi

Ono što je nedostajalo bili su pravi integracijski testovi koji prolaze kroz više slojeva odjednom:

- HTTP sloj (`MockMvc`)
- kontroler
- servis
- JPA repozitorij
- test baza (`H2`)
- centralizovani validation i error handling

Zbog toga su dodani integracijski testovi koji provjeravaju stvarno ponašanje aplikacije, a ne samo izolovane komponente.

## Osnovni pristup

Za implementaciju je korišten isti obrazac po servisima:

- `@SpringBootTest`
- `@AutoConfigureMockMvc`
- `@ActiveProfiles("test")`
- `MockMvc` za slanje stvarnih HTTP zahtjeva
- `ObjectMapper` za čitanje JSON odgovora
- repozitoriji za provjeru stanja baze nakon zahtjeva

Testovi koriste `H2` in-memory bazu kako bi bili:

- brzi
- ponovljivi
- nezavisni od lokalnog PostgreSQL servera

## Test profil i izolacija

Da bi integracijski testovi bili stabilni i lokalni, test profil je proširen tako da:

- koristi `H2` bazu
- gasi Eureku i service discovery u test okruženju

To je urađeno u sljedećim fajlovima:

- `backend/user-service/src/test/resources/application-test.yml`
- `backend/planning-service/src/test/resources/application-test.properties`
- `backend/communication-service/src/test/resources/application-test.yml`
- `backend/finance-reservation-service/src/test/resources/application-test.yml`

Za `communication-service` je dodat i test `H2` dependency u:

- `backend/communication-service/pom.xml`

## JWT i zašto je mockovan

Nakon uvođenja JWT validacije, većina endpointa očekuje `Authorization` header i provjeru tokena.

U integracijskim testovima cilj nije bio testirati kriptografsku ispravnost tokena, nego:

- da endpoint primi header
- da servis logika prođe kroz validaciju ulaza
- da se podaci stvarno upišu/pročitaju iz baze
- da error handling vrati očekivani JSON odgovor

Zato je `JwtUtils` mockovan u servisima gdje je to potrebno, kako bi test ostao fokusiran na:

- integraciju web + service + repository sloja
- a ne na generisanje ili parsiranje pravog JWT tokena

Ovo je posebno važno za:

- `planning-service`
- `communication-service`
- `finance-reservation-service`

`user-service` integracijski testovi ne trebaju JWT mock za osnovni user CRUD tok, jer se ti endpointi ne oslanjaju na JWT provjeru.

## Dodani integracijski testovi

### 1. User Service

Fajl:

- `backend/user-service/src/test/java/com/travelplanner/user_service/UserServiceIntegrationTest.java`

Pokriveno:

- kreiranje korisnika preko `POST /api/users`
- čitanje korisnika preko `GET /api/users/{id}`
- ažuriranje korisnika preko `PUT /api/users/{id}`
- brisanje korisnika preko `DELETE /api/users/{id}`
- provjera da je korisnik zaista upisan u bazu
- provjera da se password pri kreiranju ne sprema kao plain text
- provjera validation error JSON odgovora za neispravan request

Šta ovaj test dokazuje:

- da user controller, service i repository rade zajedno
- da je serializacija/deserializacija DTO-a ispravna
- da validation i global exception handler vraćaju JSON odgovor

### 2. Planning Service

Fajl:

- `backend/planning-service/src/test/java/com/travelplanner/planning_service/TravelPlanIntegrationTest.java`

Pokriveno:

- kreiranje travel plana preko `POST /api/travel-plans`
- čitanje liste planova preko `GET /api/travel-plans`
- provjera da owner dolazi iz JWT claims
- provjera da se plan stvarno snima u bazu
- validation/business rule error kada je `endDate` prije `startDate`

Šta ovaj test dokazuje:

- da controller, service i JPA rade zajedno uz JWT-aware servis logiku
- da se destination relacija pravilno koristi pri kreiranju i čitanju
- da business validation vraća strukturisan JSON error

### Važna nuspojava ovog testa

Ovaj integracijski test je otkrio pravi bug u aplikaciji:

- `GET /api/travel-plans` je bacao `LazyInitializationException`

Razlog:

- `destinationName` se mapirao nakon izlaska iz Hibernate sesije

Popravka:

- u `TravelPlanService` su `getAll(...)` i `getById(...)` označeni sa `@Transactional(readOnly = true)`

To znači da integracijski test nije samo dodan radi zadatka, nego je pomogao da se otkrije i popravi stvarni problem u runtime ponašanju servisa.

### 3. Communication Service

Fajl:

- `backend/communication-service/src/test/java/com/travelplanner/communication_service/NotificationIntegrationTest.java`

Pokriveno:

- kreiranje notifikacije preko `POST /api/notifications`
- dohvat notifikacije po ID-u
- filtriranje po korisniku preko `GET /api/notifications/user/{userId}`
- provjera da se zapis stvarno nalazi u bazi
- provjera JSON validation error odgovora za neispravan request

Šta ovaj test dokazuje:

- da notification endpointi rade end-to-end
- da persistence sloj za notifikacije stvarno funkcioniše
- da validation i global error handling vraćaju JSON

### 4. Finance Reservation Service

Fajl:

- `backend/finance-reservation-service/src/test/java/com/travelplanner/finance_reservation_service/BudgetIntegrationTest.java`

Pokriveno:

- kreiranje budžeta preko `POST /api/budgets`
- dohvat budžeta po ID-u
- dohvat budžeta po `planId`
- provjera da je budžet stvarno snimljen u bazu
- provjera JSON validation error odgovora za negativan iznos

Šta ovaj test dokazuje:

- da budget controller, budget service, mapper i repository rade zajedno
- da je JSON request/response tok ispravan
- da validacija i exception handling daju očekivan API odgovor

## Šta je svjesno ostavljeno van opsega

Ovi integracijski testovi ne testiraju:

- stvarnu Eureka registraciju
- međuservisnu mrežnu komunikaciju preko stvarnog HTTP poziva
- pravi JWT potpis i parsiranje realnog tokena
- API gateway routing end-to-end kroz više pokrenutih servisa

To je namjerno, jer bi takvi testovi više ličili na:

- system testove
- end-to-end testove
- contract testove

Za trenutni nivo zadatka fokus je bio na integraciji unutar svakog mikroservisa.

## Rezultat

Nakon dodavanja integracijskih testova:

- svi modifikovani servisi prolaze Maven testove
- backend ima pokriće ne samo na unit i MVC nivou, nego i na full-stack nivou unutar svakog servisa
- jedan realan bug u `planning-service` je detektovan i ispravljen zahvaljujući integracijskom testu

## Kratak rezime

Implementirani integracijski testovi sada provjeravaju:

- stvarne HTTP zahtjeve
- upis i čitanje iz test baze
- DTO mapiranje
- validation
- JSON error response
- osnovni business flow po servisu

Na taj način testovi daju mnogo veću sigurnost da aplikacija radi kao cjelina, a ne samo kao skup pojedinačnih klasa.
