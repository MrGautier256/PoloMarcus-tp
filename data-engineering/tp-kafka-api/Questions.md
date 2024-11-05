Voici les réponses aux questions du ReadMe :

### Kafka

#### **1. Quand devrions-nous utiliser une clé lors de la production d'un message dans Kafka ? Quels sont les risques ?**
- **Réponse** : On utilise une clé lorsque l’on veut s’assurer que les messages avec la même clé aillent dans la même partition. Cela garantit qu'ils seront traités dans l'ordre. Cependant, si une clé est utilisée de manière inégale (certaines clés beaucoup plus fréquentes que d'autres), cela peut causer un déséquilibre des partitions et affecter les performances.

#### **2. Comment fonctionne le partitionneur par défaut (sticky partition) avec Kafka ?**
- **Réponse** : Le partitionneur par défaut de Kafka est le **sticky partitioner**. Il attribue de manière aléatoire une partition à un batch de messages, puis continue d’envoyer les messages dans cette partition jusqu’à ce que le batch soit plein. Cela réduit la surcharge d'écrire à chaque fois dans différentes partitions et améliore les performances globales en batchant plus efficacement les messages dans Kafka 2.4+.

---

### Producteur

#### **1. Qu'est-ce que les sérialiseurs et désérialiseurs ? Lequel est utilisé ici ? Et pourquoi les utiliser ?**
- **Réponse** : Les **sérialiseurs** convertissent les objets en bytes avant de les envoyer à Kafka, tandis que les **désérialiseurs** transforment ces bytes en objets compréhensibles lors de la lecture. Ici, on utilise le **StringSerializer** pour les clés et valeurs car les messages sont en texte. Ils sont indispensables pour garantir que les données peuvent être lues/écrites correctement par Kafka.

#### **2. Regardez la méthode `producer.flush()` à l'intérieur de KafkaProducerService. Pouvez-vous améliorer la vitesse du programme ?**
- **Réponse** : `producer.flush()` force l’envoi des messages. Pour améliorer la vitesse, il faut éviter d’appeler cette méthode trop fréquemment. Au lieu de cela, configurer un **batching** des messages permet d'envoyer plusieurs messages en une seule requête, améliorant ainsi l’efficacité et réduisant la latence.

#### **3. Qu'en est-il du batching des messages ? Cela peut-il aider les performances de votre application ?**
- **Réponse** : Le batching combine plusieurs messages dans une seule requête réseau, réduisant la surcharge de connexion et améliorant ainsi la performance. En ajustant les paramètres de `batch.size` et `linger.ms`, il est possible de trouver un bon compromis entre latence et performance.

---

### Gestion du stockage Kafka

#### **1. Qu'en est-il de la compression des messages ? Pouvez-vous la mettre en œuvre ?**
- **Réponse** : La compression des messages permet de réduire la taille des données stockées sur les disques Kafka. On peut utiliser des algorithmes comme **Snappy**, **GZIP** ou **LZ4**. Cela réduit la charge réseau et le stockage, mais peut augmenter la consommation CPU.

#### **2. Quels sont les inconvénients de la compression ?**
- **Réponse** : Bien que la compression économise de l'espace et améliore la bande passante, elle augmente la charge sur le CPU car chaque message doit être compressé lors de l'envoi et décompressé à la lecture.

#### **3. Qu'en est-il de la durée de vie des messages sur vos brokers Kafka ? Pouvez-vous changer la configuration de votre topic ?**
- **Réponse** : La durée de rétention des messages sur Kafka peut être configurée via `log.retention.ms` ou `log.retention.bytes`. Si l'espace disque est limité, vous pouvez réduire cette durée pour éviter de saturer le stockage.

#### **4. Quels sont les inconvénients d'augmenter la durée de vie de vos messages ?**
- **Réponse** : Augmenter la durée de vie des messages consomme plus d'espace disque, ce qui peut entraîner des problèmes de stockage si les volumes de données sont importants.

---

### Qualité des données et fiabilité

#### **1. Qu'est-ce que les "acks" ? Quand utiliser `acks=0` ? Quand utiliser `acks=all` ?**
- **Réponse** : Les **acks** (acknowledgements) indiquent combien de répliques doivent confirmer la réception du message avant de considérer qu'il a été envoyé avec succès.
    - `acks=0` : Le producteur n’attend aucune confirmation. C’est rapide, mais il n’y a aucune garantie que le message a été reçu.
    - `acks=all` : Tous les réplicas In-Sync doivent accuser réception. Cela garantit une meilleure fiabilité, mais peut augmenter la latence.

#### **2. L'idempotence peut-elle nous aider ?**
- **Réponse** : Oui. **L’idempotence** garantit que les messages ne sont pas dupliqués, même en cas de réessai après une erreur. Cela est particulièrement utile lorsqu’on observe des messages en double dans le système.

#### **3. Qu'est-ce que "min.insync.replicas" ?**
- **Réponse** : Ce paramètre contrôle combien de répliques Kafka doivent être synchronisées pour qu’un message soit considéré comme validé avec `acks=all`. Cela améliore la résilience, car un nombre suffisant de répliques garantit que les messages ne sont pas perdus en cas de panne.

---

### Consommateur

#### **1. Que se passe-t-il si votre consommateur plante pendant le traitement des données ?**
- **Réponse** : Si un consommateur plante, il peut perdre la trace de l'endroit où il s'était arrêté, sauf s'il a correctement géré les **offsets**. En utilisant des commits manuels d’offsets, un consommateur peut reprendre là où il s’est arrêté.

#### **2. Quelles sont les sémantiques "at most once" / "at least once" / "exactly once" ? Que devrions-nous utiliser ?**
- **Réponse** :
    - **At most once** : Les messages peuvent être perdus, mais jamais dupliqués.
    - **At least once** : Les messages ne sont pas perdus, mais peuvent être dupliqués.
    - **Exactly once** : Les messages ne sont ni perdus ni dupliqués.

  Le choix dépend de votre application. Pour la fiabilité maximale, **exactly once** est recommandé, bien que plus coûteux en termes de performance.

---

### Schema Registry

#### **1. Quels sont les avantages d'utiliser un registre de schémas pour les messages ?**
- **Réponse** : Un **Schema Registry** permet de valider et d’assurer la compatibilité des messages dans Kafka. Il stocke les schémas des messages et garantit que les données envoyées et reçues suivent une structure cohérente.

#### **2. Où sont stockées les informations des schémas ?**
- **Réponse** : Les schémas sont stockés dans un service externe appelé **Schema Registry**, qui centralise les schémas et les versions pour tous les producteurs et consommateurs de Kafka.

#### **3. Qu'est-ce que la sérialisation ?**
- **Réponse** : La **sérialisation** est le processus de transformation d'un objet en un flux de bytes pour le stockage ou la transmission. Kafka utilise des sérialiseurs pour envoyer des messages sous forme de bytes et des désérialiseurs pour les lire.

#### **4. Quels formats de sérialisation sont supportés ?**
- **Réponse** : Kafka supporte plusieurs formats de sérialisation, dont **Avro**, **JSON**, et **Protobuf** via le Schema Registry.

#### **5. Pourquoi le format Avro est-il si compact ?**
- **Réponse** : Le format **Avro** est compact car il n'inclut pas les métadonnées (comme les noms de champs) dans chaque message. Le schéma est stocké séparément, ce qui permet de réduire considérablement la taille des messages.

#### **6. Quelles sont les meilleures pratiques pour exécuter un registre de schémas en production ?**
- **Réponse** :
    - **Haute disponibilité** : Utiliser un **cluster de Schema Registry** avec plusieurs instances pour garantir la résilience.
    - **Isolation** : Séparer le Schema Registry des autres services Kafka pour améliorer la sécurité et la gestion.

---

### Kafka Streams

#### **1. Quelles sont les différences entre les APIs du consommateur, du producteur et Kafka Streams ?**
- **Réponse** :
    - **API Producteur** : Envoie des messages vers Kafka.
    - **API Consommateur** : Lit des messages depuis Kafka.
    - **Kafka Streams** : Traite les flux de données en temps réel avec des transformations, des agrégations, et des jointures directement sur Kafka.

#### **2. Quand utiliser Kafka Streams au lieu de l'API consommateur ?**
- **Réponse** : Utilisez Kafka Streams lorsque vous avez besoin de traiter, transformer ou analyser des données en temps réel directement dans Kafka, notamment pour des applications complexes comme les agrégations ou les jointures de flux. Pour une simple lecture de données, l'API consommateur est suffisante.

#### **3. Qu'est-ce qu'un SerDe ?**
- **Réponse** : Un **SerDe** (Serializer/Deserializer) est utilisé pour sérialiser les objets en flux de bytes lors de l’écriture dans Kafka, et les désérialiser lorsqu’ils sont lus depuis Kafka.

#### **4. Qu'est-ce qu'un KStream ?**
- **Réponse** :

Un **KStream** est un flux de données dans Kafka Streams, où chaque événement est traité indépendamment des autres.

#### **5. Qu'est-ce qu'une KTable ? Qu'est-ce qu'un topic compacté ?**
- **Réponse** : Une **KTable** représente l'état agrégé d'un flux avec une vue par clé. Un **topic compacté** ne conserve que la dernière valeur par clé, permettant de réduire l’espace de stockage.

#### **6. Qu'est-ce qu'une GlobalKTable ?**
- **Réponse** : Une **GlobalKTable** est une version répliquée de KTable qui est disponible sur chaque instance Kafka Streams, utilisée pour effectuer des jointures entre flux de manière distribuée.

#### **7. Qu'est-ce qu'une opération stateful ?**
- **Réponse** : Une **opération stateful** dans Kafka Streams maintient un état entre différents événements. Par exemple, un comptage de mots ou une jointure entre deux flux nécessite de garder en mémoire des informations sur les messages précédemment traités.

