

### CRUD Reservas (OPERARIO Y ADMIN)
| RUTA | VERBO | ACCIÓN | DESCRIPCIÓN |
|----------|----------|----------|----------|
|   /reservas  |  GET   |   findAll()   |   Muestra todas las reservas disponibles  |
|   /reservas/add/:id   |  GET |   save()    |   Formulario para añadir una reserva  |  
|   /reservas/add/:id   |  POST |   save()    |   Envía el formulario para añadir una reserva  |  
|   /reservas/edit/:id   |  GET |   update()    |   Formulario para editar una reserva  | 
|   /reservas/edit/:id   |  POST |   update()    |   Envía el formulario para editar una reserva  |   
|   /reservas/del/:id   |  GET |   delete()    |   Formulario para eliminar una reserva  | 
|   /reservas/del/:id   |  POST |   delete()    |   Envía el formulario para eliminar una reserva  | 


### CRUD Reservas (USUARIO)
| RUTA | VERBO | ACCIÓN | DESCRIPCIÓN |
|----------|----------|----------|----------|
|   /mis-datos/reservas  |  GET   |   findAll()   |   Muestra todas las reservas disponibles para ese usuario |
|   /mis-datos/reservas/add/:id   |  GET |   save()    |   Formulario para añadir una reserva de ese usuario |  
|   /mis-datos/reservas/add/:id   |  POST |   save()    |   Envía el formulario para añadir una reserva de ese usuario |  
|   /mis-datos/reservas/edit/:id   |  GET |   update()    |   Formulario para editar una reserva de ese usuario | 
|   /mis-datos/reservas/edit/:id   |  POST |   update()    |   Envía el formulario para editar una reserva  |   
|   /mis-datos/reservas/del/:id   |  GET |   delete()    |   Formulario para eliminar una reserva de ese usuario | 
|   /mis-datos/reservas/del/:id   |  POST |   delete()    |   Envía el formulario para eliminar una reserva de ese usuario |


Para que el usuario no pueda realizar más de una reserva al día se le añade un disparador a nuestra base de datos.
Para controlar que no pueda reservar 7 días antes también se puede generar un disparador además de controlarlo en la vista thymeleaf.

