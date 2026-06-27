# Maven dependency conflict demo

This lab demonstrates two Maven conflict resolution rules with `slf4j-api`.

## Modules

- `lib-a`: transitively brings `org.slf4j:slf4j-api:1.7.25`
- `lib-b`: transitively brings `org.slf4j:slf4j-api:1.7.36`
- `app-short-path`: depends on `lib-a` and also directly depends on `org.slf4j:slf4j-api:2.0.9`
- `app-declaration-order-a-first`: depends on `lib-a` first, then `lib-b`
- `app-declaration-order-b-first`: depends on `lib-b` first, then `lib-a`

## What to verify

### 1. Shortest path wins

`app-short-path` has two candidates:

- direct dependency: `slf4j-api:2.0.9`
- transitive dependency through `lib-a`: `slf4j-api:1.7.25`

Maven picks `2.0.9` because the direct dependency path is shorter.

Command:

```bash
cd labs/maven-dependency-conflict-demo
mvn -pl app-short-path dependency:tree -Dverbose -Dincludes=org.slf4j:slf4j-api
```

### 2. Declaration order wins when path length is equal

`app-declaration-order-a-first` and `app-declaration-order-b-first` both see:

- `lib-a -> slf4j-api:1.7.25`
- `lib-b -> slf4j-api:1.7.36`

The path length is the same, so Maven uses the dependency that appears first in the consuming module.

Commands:

```bash
cd labs/maven-dependency-conflict-demo
mvn -pl app-declaration-order-a-first dependency:tree -Dverbose -Dincludes=org.slf4j:slf4j-api
mvn -pl app-declaration-order-b-first dependency:tree -Dverbose -Dincludes=org.slf4j:slf4j-api
```

## How to inspect a real conflict

Use these commands when production throws `NoSuchMethodError` or `ClassNotFoundException`.

1. Print the full dependency tree.

```bash
mvn dependency:tree
```

2. Narrow the tree to one artifact.

```bash
mvn dependency:tree -Dincludes=org.slf4j:slf4j-api
```

3. Show omitted nodes and conflict reason.

```bash
mvn dependency:tree -Dverbose -Dincludes=org.slf4j:slf4j-api
```

4. If needed, output to a file and grep the chain.

```bash
mvn dependency:tree -Dverbose > tree.txt
rg "slf4j-api|omitted for conflict" tree.txt
```

## How to fix after locating the conflict

- Keep one explicit version in the closest module.
- Or unify versions with `<dependencyManagement>`.
- Or exclude the unwanted transitive dependency with `<exclusions>`.
