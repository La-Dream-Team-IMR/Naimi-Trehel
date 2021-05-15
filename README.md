Naimi-Trehel

```sh
rmiregistry
```
```sh
./prisme 8080
```
```sh
javac *.java
./Launcher.sh
```
Launcher.sh lance 1 arbre de Site suivant le shéma suivant :

         1
       /  \
      2    3
     / \  / \
    4  5  6  7
    
Sinon on peut lancer un site avec la commande :
```sh
javac *.java
java Site id idParent
```
idParent vaut 0 si le Site n'a pas de parent
