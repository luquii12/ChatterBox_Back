# Comandos de Git por consola

## Preparar el proyecto:
1. Clonar el proyecto (solo se clona la **rama principal**):
   ```sh
   cd <folder path>
   git clone <project url>
   ```

2. Importarlo al IDE que se vaya a usar.

## Cómo trabajar con varias ramas:
1. Sincronizarse con las ramas remotas:
   ```sh
   git fetch --all
   ```
   
   Si es necesario actualizar la **rama_principal** en local con la remota:
   ```sh
   git checkout rama_principal
   git pull origin rama_principal
   ```

2. Si se comienza una tarea, se crea la rama correspondiente y se activa.
   ```sh
   git checkout -b rama_secundaria
   ```
   
   Si es necesario actualizar la **rama_secundaria** con las nuevas incorporaciones de la rama_principal:
   ```sh
   git checkout rama_secundaria
   git merge -m "Fusionar rama_principal con rama_secundara" rama_principal
   ```

3. Si hay conflictos toca resolverlos en la **rama_secundaria** mediante el IDE.
   Una vez se hayan resuelto los conflictos, si ha sido necesario, se realiza la tarea correspondiente en la rama.

   Al querer actualizar el repositorio hay que añadir los archivos cambiados:
   ```sh
   git add .
   ```

   Realizar el commit de los cambios:
   ```sh
   git commit -m "Mensaje con los cambios realizados"
   ```
   
   Subir los cambios a la **rama_secundaria**:
   ```sh
   git push origin rama_secundaria
   ```

4. Actualizar la **rama_principal** con los cambios de la **rama_secundaria**.
   ```sh
   git checkout rama_principal
   ```

   Primero comprobar si la **rama_principal** ha sufrido cambios en el tiempo que se ha estado trabajando en la **rama_secundaria**:
   ```sh
   git fetch origin rama_principal
   git pull origin rama_principal
   ```

   Si hay cambios se realiza de nuevo la segunda parte del punto 2, resolver conflictos si los hay y subir la **rama_secundaria** actualizada.

   Cuando se tenga la **rama_secundaria** con todos los cambios de la **rama_principal** se hace el merge:
   ```sh
   git merge --no-ff -m "Fusionar rama_secundaria con rama_principal" rama_secundaria
   ```
5. Confirmar que todo está bien y subir los cambios a remoto:
   ```sh
   git status
   git push origin rama_principal
   ```

6. Si se va a continuar trabajando en la **rama_secundaria** volver a activarla para evitar hacer cambios en la **rama_principal** cuando se vuelva a trabajar en el proyecto:
   ```sh
   git checkout rama_secundaria
   ```
   
## Otros comandos importantes:
- Comprobar el estado de la rama:
  ```sh
  git status
  ```
  A veces es importante porque puede que haya que hacer algún commit que no se tiene controlado.

- Deshacer los cambios en una rama desde el último commit:
  ```sh
  git restore
  ```
