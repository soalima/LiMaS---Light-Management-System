LiMaS - Light Management System

Proyecto Sistemas Operativos Avanzados

Integrantes del equipo

    Di Lorenzo, Maximiliano
    Gonzales Romero, Matías
    Rivero, Facundo
    Lell, Matías
    Meneghini, Maximiliano

Idea
    Sistema de administracion de luces que permite administrar todas las luces conectadas en la casa,las cuales pueden agruparse            segun los grupos que quiera el usuario, y configurarlos para diferentes ambitos.

    * En el caso de luces para exterior, las luces se fijan segun el nivel de luminiscencia en el ambiente y si hay algun usuario cerca para prenderse gradualmente, pudiendo instalarse varias luces en un pasillo que se pueden prender a medida que se camina entre ellas.

    * En el caso de luces para interior, las luces se prenden tanto por proximidad, como por comandos de sonido pregrabados. Eg: Segun el tipo de aplauso grabado para una accion, pueden prenderse todas, o selectas luces, de uno o varios colores. Pudiendo configurarse tambien un efecto de sonido cuando se prenden o apagan.

    * Opcionalmente, si se carga en la aplicación el tiempo de vida util de la nueva lampara instalada, junto a su consumo por hora, se generan estadisticas de cuanto se consumió en en dia/semana/mes automaticamente, ademas de realizar avisos estimativos en la aplicacion cuando esta cerca del limite de vida util.

   Todas las luces tienen consigo un fotosensor, parlante, sensor de proximidad y sensor de sonido. Se puede configurar un modo de funcionamiento customizado y agrupamiento de varias de estas desde la aplicacion Android, estando precargados el modo para exteriores y el de interiores. Todas las luces se conectan al arduino principal el cual lleva el control de las estadisticas y consumo, y este las envia al usuario a travez de la aplicacion.
    
Hardware a usar en el proyecto:
	* Arduino Mega 2560
	* Modulo bluetooth Hc05/06
	Por cada Nodo:
		* Modulo Sensor De Sonido Microfono Con Comparador Arduino
		* Modulo Sensor de luz ambiente LDR
		* Modulo Sensor de movimiento PIR HC-SR501
		* Buzzer Zumbador Activo De 5v
		* Luz LED   
