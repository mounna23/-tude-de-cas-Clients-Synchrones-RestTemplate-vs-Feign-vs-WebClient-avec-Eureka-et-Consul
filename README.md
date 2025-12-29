# Étude de cas : Clients Synchrones (RestTemplate vs Feign vs WebClient) avec Eureka et Consul

## Objectifs pédagogiques
À la fin du lab, il sera possible de :

- Implémenter deux microservices communiquant synchroniquement
- Configurer la découverte de services avec Eureka et avec Consul
- Implémenter 3 clients HTTP côté Service Client : RestTemplate, Feign, WebClient
- Réaliser des tests de performance (latence / débit) et collecter des métriques
- Tester la résilience (panne service voiture, panne discovery, etc.) et analyser les résultats

## Architecture cible
Services à créer :
- Service Voiture (expose l’API des voitures)
- Service Client (consomme l’API Voiture avec 3 techniques)
- Discovery : Eureka OU Consul (les deux seront testés)
Flux principal :

Service Client → (RestTemplate / Feign / WebClient) → Service Voiture
Résolution du nom logique via Eureka ou Consul

## Mise en place des microservices
<img width="1670" height="962" alt="image" src="https://github.com/user-attachments/assets/eb901973-9d35-492c-bb53-15fad97585c6" />
<img width="1670" height="992" alt="image" src="https://github.com/user-attachments/assets/b7aefb79-b413-4fc8-8246-c28ebe6a0188" />
<img width="1670" height="992" alt="image" src="https://github.com/user-attachments/assets/d6da81d7-ff7a-458a-af3d-0cf49406d20d" />
<img width="1332" height="784" alt="image" src="https://github.com/user-attachments/assets/360c1629-78e0-47c6-8461-39e91e4a8d60" />
<img width="1332" height="784" alt="image" src="https://github.com/user-attachments/assets/c96e01b1-a617-4cea-b374-6d0f8e7774d2" />
<img width="1332" height="784" alt="image" src="https://github.com/user-attachments/assets/9bb5d860-2b4d-426c-a2dc-cc72aed3bc5d" />

## Livrable :

- Tableau 1 — Performance (latence et débit)
  Eureka
  <img width="1582" height="498" alt="image" src="https://github.com/user-attachments/assets/f61199ac-6215-4bfb-843a-4cc075d2f26c" />
  Consul
  <img width="1522" height="498" alt="image" src="https://github.com/user-attachments/assets/763aa6b7-6e7a-4e9d-b3df-5227adcfc652" />
  
- Tableau 2 — CPU / Mémoire
  Eureka
  <img width="1516" height="472" alt="image" src="https://github.com/user-attachments/assets/1e01732c-a8a3-4dfb-abca-eaeca8ac1d18" />
  Consul
<img width="1516" height="472" alt="image" src="https://github.com/user-attachments/assets/0e6c7d3a-2731-419a-86c5-4fc1a13d23bc" />

- Tableau 3 — Résilience
  <img width="1516" height="550" alt="image" src="https://github.com/user-attachments/assets/2a125e48-5555-40c3-b9c3-ac572f408820" />
- Tableau 4 — Simplicité
  <img width="1516" height="474" alt="image" src="https://github.com/user-attachments/assets/3c37d00c-3e6c-44f1-bffa-1f4c1df3d093" />
  
## Discussion & limites

### 1) Discussion des résultats

Les résultats mettent en évidence deux axes principaux :
Impact du client HTTP (RestTemplate / Feign / WebClient)
Dans l’ensemble, WebClient obtient les meilleures performances (latence plus faible, débit plus élevé) et une consommation CPU/RAM plus contenue, principalement grâce à son modèle réactif et non bloquant.
À l’inverse, RestTemplate et Feign reposent généralement sur un modèle bloquant, ce qui peut limiter la scalabilité sous forte charge, surtout si le nombre de threads devient un facteur limitant.
Impact de la solution de discovery (Eureka / Consul)
Consul peut offrir une détection plus rapide des instances indisponibles via ses mécanismes de health checks et sa logique orientée service registry.
Eureka, de son côté, montre souvent une bonne tolérance aux pannes du serveur de discovery grâce à des comportements de cache côté client, permettant de maintenir temporairement une résolution d’instances même si le serveur est indisponible (selon la config et la durée de validité des informations).
En synthèse, pour un environnement orienté performance et montée en charge :
WebClient se démarque comme meilleur choix côté client HTTP.
Le choix Eureka vs Consul dépend davantage des besoins en fonctionnalités, observabilité, et modes de déploiement que de la performance pure.

### 2) Interprétation et compromis

Performance vs Simplicité
- RestTemplate : simple mais moins optimisé et plutôt “legacy” dans l’écosystème Spring moderne.
- Feign : très productif (déclaratif) mais ajoute une couche d’abstraction et parfois un overhead.
- WebClient : performant, mais demande une meilleure maîtrise (réactif, gestion backpressure, etc.).
- Résilience et comportement en panne
- Les différences observées pendant les pannes (service down / discovery down) dépendent fortement de :
   - la configuration des timeouts,
   - la stratégie de retry/circuit breaker (si mise en place),
   - les TTL / cache registry côté client,
   - les health checks côté Consul.
Ainsi, un mauvais paramétrage (timeouts trop longs, retries mal calibrés) peut dégrader fortement l’expérience perçue, même avec un “bon” choix technologique.

### 3) Limites de l’étude

Cette étude présente plusieurs limites qui peuvent influencer les résultats :
- Environnement de test
Les performances varient fortement selon :
  - machine (CPU, RAM),
  - exécution locale vs conteneur (Docker),
  - réseau (latence, DNS),
  - OS et JDK.
- Paramètres de charge
Les métriques dépendent de :
nombre de requêtes,
niveau de concurrence,
durée du test (warm-up, stabilisation JVM),
taille des payloads et complexité du traitement.
Absence (ou présence) d’optimisations transverses
- Sans mécanismes comme :
pooling de connexions,
tuning du thread pool,
compression,
cache applicatif,
les résultats peuvent sous-estimer le potentiel réel.
Résilience partiellement représentée
Les scénarios de panne testés (service-client, service-voiture, discovery) ne couvrent pas :
pannes intermittentes (flapping),
saturation CPU/RAM,
timeouts réseau partiels,
erreurs 5xx en rafale,
partitions réseau (cas réel fréquent en microservices).
Comparaison simplifiée Eureka vs Consul
Eureka et Consul ne jouent pas toujours exactement le même rôle “out of the box” (ex: health checks natifs, agent Consul, intégrations). Une comparaison 100% équitable exige une configuration équivalente et des scénarios identiques.

### 4) Pistes d’amélioration

Pour renforcer la validité et la reproductibilité :
Ajouter un warm-up JVM (ex: 30–60s) et répéter chaque test N fois (moyenne + écart-type).
Standardiser les paramètres : timeouts, retries, pooling, concurrency.
Ajouter de la résilience (si non fait) : Resilience4j (timeout, retry, circuit breaker, bulkhead).
Tester en conditions proches prod :
multi-instances, latence réseau simulée (tc/netem), charge plus longue, monitoring (Prometheus/Grafana).
Publier un fichier “Test protocol” (comment lancer les tests, commandes, configs, versions).

### 5) Conclusion de discussion

Dans le cadre de cette étude :
WebClient apparaît comme le meilleur candidat pour des charges élevées et une meilleure gestion des ressources.
Feign reste un excellent compromis productivité/maintenabilité, surtout si la charge est modérée et la lisibilité prioritaire.
Eureka convient bien pour une mise en place rapide dans l’écosystème Spring Cloud, tandis que Consul devient intéressant lorsqu’on recherche davantage de fonctionnalités de discovery/health et d’intégration infra.

## Conclusion générale

Cette étude a comparé RestTemplate, Feign et WebClient dans une architecture microservices basée sur Spring Boot, en utilisant Eureka et Consul pour la découverte de services. Les résultats montrent que WebClient offre les meilleures performances et une consommation de ressources plus faible grâce à son modèle non bloquant. Feign se distingue par sa simplicité et sa productivité, tandis que RestTemplate reste une solution fonctionnelle mais moins adaptée aux applications modernes à forte charge.
Du côté du service discovery, Eureka se démarque par sa facilité d’intégration et sa tolérance aux pannes, alors que Consul propose une détection plus rapide des services indisponibles et des fonctionnalités plus avancées. Le choix final dépend donc des besoins en performance, en simplicité et en résilience du système.
