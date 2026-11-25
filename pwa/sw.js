const CACHE_NAME = "frozen-anchor-v3";

const CORE_ASSETS = [
    "./",
    "./index.html",
    "./app.js?v=13",
    "./manifest.json"
];

const REFRIGERANTS = [
    "R11", "R12", "R22", "r23", "R32", "R114", "R123", "R124", "R134A", "R245FA",
    "R290", "R404A", "R407A", "R407C", "R407F", "R410A", "R422B", "R422D", "R438A",
    "R448A", "R449A", "R452A", "R452B", "R454A", "R454B", "R454C", "R502", "R507A",
    "R513A", "R600A", "R744", "R1234YF", "R1234ZE", "R1270"
];

const ALL_ASSETS = [
    ...CORE_ASSETS,
    ...REFRIGERANTS.map(r => `./main/resources/refrigerants/${r}.tsv`)
];

self.addEventListener("install", (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME).then((cache) => cache.addAll(ALL_ASSETS))
    );
    self.skipWaiting();
});

self.addEventListener("activate", (event) => {
    event.waitUntil(
        caches.keys().then((keys) =>
            Promise.all(
                keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key))
            )
        )
    );
    self.clients.claim();
});

self.addEventListener("fetch", (event) => {
    event.respondWith(
        caches.match(event.request, { ignoreSearch: true }).then((cached) => {
            return cached || fetch(event.request);
        })
    );
});