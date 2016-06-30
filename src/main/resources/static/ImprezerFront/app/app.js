var app = angular.module('Imprezer', ['ngMaterial', 'ngMdIcons', 'mdPickers']);

app.config(
    function ($locationProvider, $httpProvider) {
        $locationProvider.html5Mode({
            enabled: true
        });

        $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
    });

app.controller('AppCtrl', ['$scope', '$mdBottomSheet', '$mdSidenav', '$mdDialog', '$http', '$location', '$window',
    function ($scope, $mdBottomSheet, $mdSidenav, $mdDialog, $http, $location, $window) {
        $scope.listContentLabelValue = 'BEST EVENTS';
        //Poland center
        $scope.DEAFULT_LATITUDE = 51.919438;
        $scope.DEAFULT_LONGITUDE = 19.145136;

        $scope.toggleSidenav = function (menuId) {
            $mdSidenav(menuId).toggle();
        };

        $scope.showDetails = function (event) {
            console.log("EVENT CLICKED!");
            console.log(event);
            $http.get("/events/" + event.id + "/details")
                .success(function (data) {
                    $scope.showDetailsDialog(event, data);
                })
                .error(function (data) {
                    console.log(data);
                    console.log("Receiving details error.");
                });
        };

        $scope.showDetailsDialog = function (event, details) {
            $scope.getFixedPosition = function () {
                return details.location;
            };
            $mdDialog.show({
                controller: 'DetailsController',
                templateUrl: 'templates/details.tmpl.html',
                clickOutsideToClose: true,
                onComplete: $scope.detailsSetUp,
                locals: {
                    showMap: $scope.showMap,
                    getLastMarker: $scope.getLastMarker,
                    updatePosition: $scope.updatePosition,
                    event: event,
                    details: details
                }
            });

        };
        $scope.detailsSetUp = function () {
            $scope.loadMap($scope.getFixedPosition);
            $scope.getFixedPosition = undefined;
        }
        // setup authentication details
        $http.get("/user").success(function (data) {
            console.log("Login request success!");
            console.log(data);
            if (data.name) {
                console.log("User logged in!");
                $scope.user = data;
                $scope.authenticated = true;
            } else {
                console.log("User NOT logged in!");
                $scope.user = {
                    name: "N/A"
                };
                $scope.authenticated = false;
            }
        }).error(function (data) {
            console.log("Login error!");
            console.log(data);
            $scope.user = "N/A";
            $scope.authenticated = false;
        });
        $scope.isAuthenticated = function () {
            return $scope.authenticated;
        };

        //set up logout function
        $scope.logout = function () {
            $http.post('logout', {}).success(function () {
                $scope.authenticated = false;
                $window.location.href = '/'
            }).error(function (data) {
                console.log("Logout failed: ");
                console.log(data);
                $scope.authenticated = false;
            });
        };

        //set up the login function
        $scope.login = function () {
            $window.location.href = '/login/facebook'
            // $location.path("/login/facebook");
        };


        //model feeders

        $scope.menu = [
            {
                link: '',
                title: 'Dashboard',
                icon: 'settings'
            },
            {
                link: '',
                title: 'Friends',
                icon: 'settings'
            },
            {
                link: '',
                title: 'asda',
                icon: 'settings'
            }
        ];

        $scope.admin = [
            {
                link: '',
                title: 'Logout',
                icon: 'logout',
                action: $scope.logout
            }
        ];

        $scope.allYesNo = [
            {
                value: 0,
                abbrev: 'Doesn\'t matter'
            },
            {
                value: 1,
                abbrev: 'Yes'
            },
            {
                value: -1,
                abbrev: 'No'
            }

        ];
        $scope.yesNo = [
            {
                value: 1,
                abbrev: 'Yes'
            },
            {
                value: -1,
                abbrev: 'No'
            }

        ];
        //Filter values
        $scope.filter_alcoholValue = 0;
        $scope.filter_paidEntrance = 0;
        $scope.filter_includedCategories = [];
        $scope.filter_excludedCategories = [];

        $scope.notExcluded = function (element) {
            for (var i = 0; i < $scope.filter_excludedCategories.length; i++) {
                if (element.value == $scope.filter_excludedCategories[i]) {
                    return false;
                }
            }
            return true;

        };
        $scope.notIncluded = function (element) {
            for (var i = 0; i < $scope.filter_includedCategories.length; i++) {
                if (element.value == $scope.filter_includedCategories[i]) {
                    return false;
                }
            }
            return true;

        };

        $scope.formatDateTimes = function (events) {
            for (var i = 0; i < events.length; i++) {
                events[i].beginDateTime = new Date(events[i].beginDateTime).toLocaleString();
                events[i].endDateTime = new Date(events[i].endDateTime).toLocaleString();
            }
        };

        // setup authentication details
        $http.get("/events?scope=best").success(function (data) {
            console.log("Promoted events retrieved!");
            console.log(data);
            if (data.content !== undefined && data.content.length > 0) {
                // if pageable
                $scope.formatDateTimes(data.content);
                $scope.activity = data.content;
            } else if (data.length !== undefined && data.length > 0) {
                // if raw events
                $scope.formatDateTimes(data);
                $scope.activity = data;
            }
            else {
                console.log("Events not retrieved!");
                $scope.activity = [];
            }
        }).error(function (data) {
            $scope.activity = [];
        });

        $http.get("/categories").success(function (data) {
            console.log("Categories received!");
            console.log(data);
            if (data.length > 0) {
                $scope.categories = data;
            } else {
                console.log("Categories not retrieved!");
                $scope.categories = [];
            }
        }).error(function (data) {
            console.log("Error while getting categories");
            $scope.categories = [];
        });

        $scope.announceClick = function (index) {
            $mdDialog.show(
                $mdDialog.alert()
                    .title('You clicked!')
                    .textContent('You clicked the menu item at index ' + index)
                    .ok('Nice')
            );
        };
        $scope.applyFilters = function () {
            console.log("HERE IT IS:");
            console.log("Value of: alcohol");
            console.log($scope.filter_alcoholValue);
            console.log("Value of: paid entr");
            console.log($scope.filter_paidEntrance);
            console.log("Value of: date");
            console.log($scope.filter_datetime);
            console.log("Value of: range");
            console.log($scope.filter_range);
            console.log("Value of: longitude");
            console.log($scope.filter_longitude);
            console.log("Value of: latitiude");
            console.log($scope.filter_latitude);
            console.log("Value of: included cats");
            console.log($scope.filter_includedCategories);
            console.log("Value of: exluded cats");
            console.log($scope.filter_excludedCategories);


            var request = "/events?scope=filter";

            //name
            if ($scope.filter_name !== undefined && $scope.filter_name !== null) {
                request += "&name=" + $scope.filter_name;
            }
            //time
            if ($scope.filter_datetime !== undefined && $scope.filter_datetime !== null) {
                request += "&datetime="
                    + $scope.filter_datetime.toLocaleDateString()
                    + ":"
                    + $scope.filter_datetime.toLocaleTimeString()
                    + ":"
                    + $scope.filter_datetime.getTimezoneOffset();
            }
            //place
            if ($scope.filter_longitude !== undefined && $scope.filter_longitude !== null) {
                request += "&lng=" + $scope.filter_longitude;
            }
            if ($scope.filter_latitude !== undefined && $scope.filter_latitude !== null) {
                request += "&lat=" + $scope.filter_latitude;
            }
            if ($scope.filter_range !== undefined && $scope.filter_range !== null) {
                request += "&range=" + $scope.filter_range;
            }
            //categories
            if ($scope.filter_includedCategories.length !== 0) {
                request += "&inclCats=" + $scope.filter_includedCategories;
            }
            if ($scope.filter_excludedCategories.length !== 0) {
                request += "&exclCats=" + $scope.filter_excludedCategories;
            }
            //details
            if ($scope.filter_alcoholValue !== 0) {
                request += "&alcohol=" + $scope.filter_alcoholValue;
            }
            if ($scope.filter_paidEntrance !== 0) {
                request += "&paid=" + $scope.filter_paidEntrance;
            }

            $scope.request = request;
            $http.get(request).success(function (data) {
                console.log("Promoted events retrieved!");
                console.log(data);
                $scope.listContentLabelValue = 'FILTERED EVENTS';
                if (data.content !== undefined && data.content.length > 0) {
                    // if pageable
                    $scope.formatDateTimes(data.content);
                    $scope.activity = data.content;
                    $location.url(request);
                } else if (data.length !== undefined && data.length > 0) {
                    // if raw events
                    $scope.formatDateTimes(data);
                    $scope.activity = data;
                    $location.url(request);
                } else {
                    $scope.listContentLabelValue = 'No events found';
                    console.log("Events not retrieved!");
                    $scope.activity = [];
                }
            }).error(function (data) {
                $scope.listContentLabelValue = 'No events found';
                $scope.activity = [];
            });
        };

        $scope.updatePosition = function (latitude, longitude) {
            $scope.filter_latitude = latitude;
            $scope.filter_longitude = longitude;
        };

        $scope.clearMarker = function () {
            $scope.deleteOverlays();
            $scope.lastMarker = undefined;

        };

        $scope.getLastMarker = function () {
            return $scope.lastMarker;
        };


        $scope.placeMarker = function (location) {
            // first remove all markers if there are any
            $scope.deleteOverlays();

            var marker = new google.maps.Marker({
                position: location,
                map: $scope.map
            });

            // add marker in markers array
            $scope.lastMarker = marker;

        };

// Deletes all markers in the array by removing references to them
        $scope.deleteOverlays = function () {
            if ($scope.lastMarker) {
                $scope.lastMarker.setMap(null);
                $scope.lastMarker = undefined;
            }
        };

        $scope.loadMap = function () {
            $scope.loadMap(undefined);
        };
        $scope.loadMap = function (getFixedPosition) {
            if (getFixedPosition !== undefined && typeof getFixedPosition == 'function') {
                var fixedPosition = getFixedPosition();
            }

            var mapOptions;
            if (fixedPosition !== undefined) {
                mapOptions = {
                    mapTypeId: google.maps.MapTypeId.ROADMAP,
                    zoom: 10,
                    center: new google.maps.LatLng(fixedPosition[0], fixedPosition[1])
                };
            } else {
                mapOptions = {
                    mapTypeId: google.maps.MapTypeId.ROADMAP,
                    zoom: 4,
                    center: new google.maps.LatLng($scope.DEAFULT_LATITUDE, $scope.DEAFULT_LONGITUDE)
                };
            }


            var popUpContent = document.getElementById('map_container');
            var mapView = document.getElementById('map');
            var popUpHeight = popUpContent.offsetHeight;
            var style = "width: 100%; ";
            if (popUpHeight !== 0){
                style += " min-height:" + popUpHeight + "px;";
            }
            mapView.setAttribute("style", style);
            $scope.map = new google.maps.Map(mapView, mapOptions);

            if (fixedPosition !== undefined) {
                $scope.placeMarker({lat: fixedPosition[0], lng: fixedPosition[1]});
            } else {
                var marker = $scope.getLastMarker();

                if (marker !== undefined) {
                    $scope.placeMarker(marker.position);
                }

                google.maps.event.addListener($scope.map, "click", function (event) {
                    // place a marker
                    $scope.placeMarker(event.latLng);
                });
            }
        }

        $scope.showMap = function (location) {
            $mdDialog.show({
                controller: 'MapController',
                templateUrl: 'templates/map.tmpl.html',
                clickOutsideToClose: true,
                onComplete: $scope.loadMap,
                locals: {
                    updatePosition: $scope.updatePosition,
                    getLastMarker: $scope.getLastMarker,
                    clearMarker: $scope.clearMarker
                }
            });

        };

        $scope.showAdd = function (ev) {
            $mdDialog.show({
                controller: 'AddEventController',
                templateUrl: 'templates/add_view.tmpl.html',
                targetEvent: ev,
                onComplete: $scope.loadMap,
                locals: {
                    categories: $scope.categories,
                    getLastMarker: $scope.getLastMarker
                }
            });
        };

        $scope.alert = '';

        $scope.showListBottomSheet = function ($event) {
            $scope.alert = '';
            $mdBottomSheet.show({
                templateUrl: 'templates/list_bottom_sheet.tmpl.html',
                controller: 'ListBottomSheetCtrl',
                targetEvent: $event
            }).then(function (clickedItem) {
                $scope.alert = clickedItem.name + ' clicked!';
            });
        };

    }
])
;
app.controller('MapController', function ($scope, $mdDialog, updatePosition, getLastMarker, clearMarker) {
    $scope.updatePosition = updatePosition;
    $scope.getLastMarker = getLastMarker;
    $scope.clearMarker = clearMarker;

    $scope.hideDialog = function () {
        $mdDialog.hide();
    };

    $scope.savePlace = function () {
        if ($scope.getLastMarker() !== undefined) {
            var latlng = $scope.getLastMarker();
            $scope.updatePosition(latlng.position.lat(), latlng.position.lng());
        }

        $mdDialog.hide()
    };
});

app.controller('DetailsController', function ($scope, $mdDialog, showMap, getLastMarker, updatePosition, event, details) {
    $scope.mapVisible = false;
    $scope.showMap = showMap;
    $scope.getLastMarker = getLastMarker;
    $scope.updatePosition = updatePosition;
    $scope.event = event;
    $scope.details = details;


    $scope.toggleMap = function () {
        console.log("Map visible: " + $scope.mapVisible);
        $scope.mapVisible = !$scope.mapVisible;
        setTimeout(function () {
            google.maps.event.trigger(map, 'resize');
        }, 100);

    };

    //functions prepared for showing the map

    $scope.hideDialog = function () {
        $mdDialog.hide();
    };
});

app.controller('ListBottomSheetCtrl', function ($scope, $mdBottomSheet) {
    $scope.items = [
        {name: 'Tmp', icon: 'share', action: $scope.logout}
    ];

    $scope.listItemClick = function ($index) {
        var clickedItem = $scope.items[$index];
        $mdBottomSheet.hide(clickedItem);
    };
});

app.controller('AddEventController', function ($scope, $http, $mdDialog, categories, getLastMarker) {
    $scope.categories = categories;
    $scope.getLastMarker = getLastMarker;
    // $scope.EMPTY = '';
    $scope.addedEvent = {};
    $scope.addedEvent.categories = [];

    $scope.cancel = function () {
        $mdDialog.hide();
    };
    $scope.verifyAndAddEvent = function () {
        console.log($scope.addedEvent);

        $scope.showError = function (msg) {
            $scope.addEventError = msg;
            var errMsg = document.getElementById('error_message');
            if (errMsg) {
                errMsg.focus();
            }
        }
        $scope.clearError = function () {
            $scope.addEventError = undefined;
        };
        var errorMessage = "";
        $scope.clearError();
        //check name
        if ($scope.addedEvent.name === undefined) {
            errorMessage += "Adding event: name not provided.\n";
        }
        //check categories
        //TODO: limit amount of categories to 3!
        if ($scope.addedEvent.categories.length === 0) {
            errorMessage += "Adding event: categories list is empty.\n";
        }
        //begin datetime
        if (!$scope.addedEvent.begin_date) {
            errorMessage += "Adding event: begin date not provided.\n";
        }
        // if (!$scope.addedEvent.begin_time) {
        //     errorMessage += "Adding event: bagin_time not provided.\n";
        // }
        //end datetime
        if (!$scope.addedEvent.end_date) {
            errorMessage += "Adding event: end_date not provided.\n";
        }

        // if (!$scope.addedEvent.end_time) {
        //     errorMessage += "Adding event: end_time not provided.\n";
        // }
        //true false params
        if (!$scope.addedEvent.alcohol) {
            $scope.addedEvent.alcohol = false;
            errorMessage += "Adding event: alcohol value not provided.\n";
        }
        if (!$scope.addedEvent.paidEntrance) {
            $scope.addedEvent.paidEntrance = false;
            errorMessage += "Adding event: paidEntrance not provided.\n";
        }
        //place
        if ($scope.getLastMarker) {
            var latlng = $scope.getLastMarker();
            if (latlng) {
                $scope.addedEvent.latitude = latlng.position.lat();
                $scope.addedEvent.longitude = latlng.position.lng();
            }
        }
        if ($scope.addedEvent.latitude === $scope.EMPTY) {
            errorMessage += "Adding event: latitude not provided.\n";
        }

        if ($scope.addedEvent.longitude === $scope.EMPTY) {
            errorMessage += "Adding event: longitude not provided.\n";
        }

        if (!$scope.addedEvent.place) {
            errorMessage += "Adding event: place not provided.\n";
        }
        if (errorMessage !== "") {
            $scope.showError(errorMessage);
        }

        // $scope.addedEvent.begin_date.setTime($scope.addedEvent.begin_time)
        // $scope.addedEvent.end_date.setTime($scope.addedEvent.end_time)
        $http.post("/events", {
            name: $scope.addedEvent.name,
            beginDateTime: $scope.addedEvent.begin_date,
            endDateTime: $scope.addedEvent.end_date,
            location: [
                $scope.addedEvent.latitude,
                $scope.addedEvent.longitude
            ],
            place: $scope.addedEvent.place,
            categories: $scope.addedEvent.categories,
            alcohol: $scope.addedEvent.alcohol,
            paid: $scope.addedEvent.paidEntrance,
            details: $scope.addedEvent.details
        }).success(function (data) {
                $scope.clearError();
                $mdDialog.hide();
            })
            .error(function (data) {
                console.log("Adding event failed: ");
                console.log(data);
                $scope.showError(data.message);
                $scope.authenticated = false;
            });
    };
});

app.directive('userAvatar', function () {
    return {
        replace: true,
        templateUrl: 'templates/user_avatar.tmpl.html'
    };
});

app.config(function ($mdThemingProvider) {
    var customBlueMap = $mdThemingProvider.extendPalette('blue-grey', {
        'contrastDefaultColor': 'light',
        'contrastDarkColors': ['50'],
        'contrastLightColors': ['500'],
        '50': 'ffffff'
    });
    $mdThemingProvider.definePalette('customBlue', customBlueMap);
    $mdThemingProvider.theme('default')
        .primaryPalette('customBlue', {
            'default': '500'
        })
        .accentPalette('light-green');

    $mdThemingProvider.theme('docs-dark', 'default')
        .primaryPalette('light-green')
        .dark();
});
