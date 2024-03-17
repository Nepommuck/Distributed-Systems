const baseUrl = "http://localhost:8080";

function getWeather(locationName) {
  return fetch(`${baseUrl}/weather/${locationName}`).then((response) => {
    if (!response.ok) {
      throw new Error(`Request failed with status: ${response.status}`);
    }
    return response.json();
  });
}

function getAllLocations() {
  return fetch(`${baseUrl}/locations`).then((response) => {
    if (!response.ok) {
        throw new Error(`Request failed with status: ${response.status}`);
    }
    return response.json();
  });
}

function addLocation(name, lon, lat) {
  return fetch(`${baseUrl}/locations?name=${name}&lat=${lat}&lon=${lon}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
  }).then((response) => {
    if (!response.ok) {
        throw new Error(`Request failed with status: ${response.status}`);
    }
    return response.json();
  });
}

function deleteLocation(id) {
    return fetch(`${baseUrl}/locations/${id}`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
      },
    }).then((response) => {
      if (!response.ok) {
          throw new Error(`Request failed with status: ${response.status}`);
      }
      return response.json();
    });
}

function displayLocations() {
  const newRecord = (id, name, coordinates) =>
    `<div class="location">
        <a class="name">${name}</a>
        <a class="temp">Latitude: ${coordinates.lat}</a>
        <a class="temp">Longitude: ${coordinates.lon}</a>
        <button class="remove" onclick="removeLocation(${id})">Remove</button>
    </div>`;

  let resultHtlm = "";
  getAllLocations()
    .then((allLocations) => {
      allLocations
        .slice()
        .sort((a, b) => a.name.localeCompare(b.name))
        .forEach((location) => {
          resultHtlm += newRecord(location.id, location.name, location.coordinates);
        });
      document.getElementById("location-list").innerHTML = resultHtlm;
    })
    .catch((error) => {
      console.error(error);
    });
}

function addNewLocation() {
  const form = document.getElementById("add-location");
  const locationName = form.elements["location-name"].value;
  const latitude = form.elements["latitude"].value;
  const longitude = form.elements["longitude"].value;

  addLocation(locationName, latitude, longitude).then((_) => {
      displayLocations();
  }).catch(error =>{
    displayError(error)
  });
}

function removeLocation(id) {
    deleteLocation(id).then(_ => {
        displayLocations()
    })
}

function sumbitWeatherForm() {
  const form = document.getElementById("get-weather-form");
  const location = form.elements["location"].value;

  getWeather(location)
    .then((response) => {
      console.log(response);
      showWeatherForecast(response);
    })
    .catch((error) => {
      displayError(error);
    });
}

function showWeatherForecast(weatherData) {
  const newRecord = (date, temperatureData, numberOfIntegrations) =>
    `<div class="record">
        <a class="date">${date}</a>
        <a class="temp">Avg: ${temperatureData.averageTemperature.celsius}°C</a>
        <a class="temp">Min: ${temperatureData.minTemperature.celsius}°C</a>
        <a class="temp">Max: ${temperatureData.maxTemperature.celsius}°C</a>
        <a class="integrations">Data from<br>${numberOfIntegrations} integration${
      numberOfIntegrations > 1 ? "s" : ""
    }</a>
  </div>`;

  console.log(weatherData);
  let resultHtlm = "";
  Object.keys(weatherData.dailyForecasts).forEach((date) => {
    const dailyForecast = weatherData.dailyForecasts[date];
    resultHtlm += newRecord(
      date,
      dailyForecast.dailyTotal,
      dailyForecast.allData.length
    );
  });
  document.getElementById("weather-results").innerHTML = resultHtlm;
}

function displayError(message) {
  alert(message);
}
