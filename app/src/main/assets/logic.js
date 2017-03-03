// карта
var myMap;
// марка
var placeMark;
// флаг сущестования марки на карте
var placeMarkFlag = false;
// маркас моим местоположением
var placeMarkIam;
// флаг марки с моим местоположением
var placeMarkIamFlag = false;
// массив для расстояний
var distanceArray = [];
// текущая заполненость выходного массива с расстояниями
var foundItems = 0;
// размерность выходного массива
var distanceArrayLength;
// вспомогательная переменная хранящая координаты
var coords;

// Перейти к данному месту, ставить балун и марку
function goToPlace(id, comment, latitude, longitude) {
    if (placeMarkFlag) {
        myMap.geoObjects.remove(placeMark);
    }

    if (placeMarkIamFlag) {
        myMap.geoObjects.remove(placeMarkIam);
        placeMarkIamFlag = false;
    }


    placeMark = new ymaps.Placemark([latitude, longitude], null, {
        preset: 'islands#circleIcon',
        iconColor: '#3caa3c'
    })
    myMap.geoObjects.add(placeMark);
    placeMarkFlag = true;
    if (myMap.balloon.isOpen()) {
        myMap.balloon.close();
    }
    myMap.balloon.open([latitude, longitude], {
        panelMaxMapArea: 0,
        contentHeader: 'Порядковый номер ' + id,
        contentBody: comment,
    });

}

// Удалить с карты балун и марку
function deletePlaceFromMap() {

    if (placeMarkFlag) {
        myMap.geoObjects.remove(placeMark);
        placeMarkFlag = false;
    }

    if (placeMarkIamFlag) {
        myMap.geoObjects.remove(placeMarkIam);
        placeMarkIamFlag = false;
    }

    if (myMap.balloon.isOpen()) {
        myMap.balloon.close();
    }
}

// Определения расстояния до массива точек
function searchDistance(coordsArray) {
    if (placeMarkIamFlag) {
        myMap.geoObjects.remove(placeMarkIam);
    }
    var userCoords;
    distanceArrayLength = coordsArray.length / 2;
    ymaps.geolocation.get({
        provider: 'auto'
    }).then(function(result) {
        userCoords = result.geoObjects.get(0).geometry.getCoordinates();
        placeMarkIam = new ymaps.Placemark(userCoords, {
            iconCaption: 'Вы здесь!'
        }, {
            preset: 'islands#greenDotIconWithCaption'
        });
        myMap.panTo(userCoords, {
                          flying: true
                      });
        myMap.geoObjects.add(placeMarkIam);
        placeMarkIamFlag = true;
        for (var i = 0; i < coordsArray.length; i += 2) {
            getDistance(i / 2, userCoords, coordsArray[i], coordsArray[i + 1]);
        }
    });
}

// Определения расстояния до массива точек с известным начальным положением
function searchDistance2(coordsArray, latitude, longitude) {
    if (placeMarkIamFlag) {
        myMap.geoObjects.remove(placeMarkIam);
    }
    var userCoords = [latitude, longitude];
    distanceArrayLength = coordsArray.length / 2;
    placeMarkIam = new ymaps.Placemark(userCoords, {
            iconCaption: 'Вы здесь!'
    }, {
            preset: 'islands#greenDotIconWithCaption'
    });
    myMap.panTo(userCoords, {
                          flying: true
    });
    myMap.geoObjects.add(placeMarkIam);
    placeMarkIamFlag = true;
    for (var i = 0; i < coordsArray.length; i += 2) {
            getDistance(i / 2, userCoords, coordsArray[i], coordsArray[i + 1]);
    };
}


// Находим расстоние для каждой конкретной точки
function getDistance(i, userCoords, coordsArray1, coordsArray2) {
    ymaps.route([
        [userCoords[0], userCoords[1]],
        [coordsArray1, coordsArray2]
    ]).then(
        function(route) {
            distanceArray[i] = route.getLength();
            checkProgress();
        },
        function(error) {
            distanceArray[i] = 0;
            checkProgress();
        }
    );
}

// Проверяем готовность массива с выходными данными
function checkProgress() {
    foundItems++;
    if (foundItems == distanceArrayLength) {
        distanceArrayLength = 0;
        foundItems = 0;
        for (var i = 0; i < distanceArray.length; i += 1) {
            distanceArray[i] = '' + distanceArray[i];
        }
        Android.sortByDistance(distanceArray);
    }
}



// Поставить балун и марку
function makeBalloon(newId, latitude, longitude, address) {
    if (placeMarkFlag) {
        myMap.geoObjects.remove(placeMark);
    }
    if (placeMarkIamFlag) {
        myMap.geoObjects.remove(placeMarkIam);
        placeMarkIamFlag = false;
    }
    if (myMap.balloon.isOpen()) {
        myMap.balloon.close();
    }

    placeMark = new ymaps.Placemark(coords, null, {
        preset: 'islands#violetCircleDotIconWithCaption'
    })
    myMap.geoObjects.add(placeMark);
    placeMarkFlag = true;

    myMap.balloon.open(coords, {
        panelMaxMapArea: 0,
        contentHeader: 'Порядковый номер ' + newId,
        contentBody: 'Обычный комментарий'
    });
    Android.createNewPlaceItem(address, latitude, longitude);
}


// Дождёмся загрузки API и готовности DOM.
ymaps.ready(init);

function init() {


    // Создание экземпляра карты и его привязка к контейнеру с
    // заданным id ("map").
    myMap = new ymaps.Map('map', {
        // При инициализации карты обязательно нужно указать
        // её центр и коэффициент масштабирования.
        center: [56.35, 44], // НН
        zoom: 9,
        controls: ['zoomControl']
    }, {
        searchControlProvider: 'yandex#search'
    });

    // Событие клика на карту
    myMap.events.add('click', function(e) {
        coords = e.get('coords');
        var newId = Android.getNextId();
        var myGeocoder = ymaps.geocode(coords, {
            results: 1
        });
        var latitude = coords[0].toPrecision(6);
        var longitude = coords[1].toPrecision(6);
        var address;
        myGeocoder.then(
            function(res) {
                if (res.geoObjects.get(0)) {
                    address = res.geoObjects.get(0).properties.get('name');
                } else {
                    address = 'Не удалось определить адрес.';
                }
                makeBalloon(newId, latitude, longitude, address);
            });
    });

}