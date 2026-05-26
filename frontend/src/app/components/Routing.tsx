import { useEffect } from "react";
import { useMap } from "react-leaflet";
import L from "leaflet";

import "leaflet-routing-machine";
import "leaflet-routing-machine/dist/leaflet-routing-machine.css";

type Stop = {
  latitude: number;
  longitude: number;
};

type Props = {
  stops: Stop[];
};

export default function Routing({ stops }: Props) {
  const map = useMap();

  useEffect(() => {
    if (!stops || stops.length < 2) return;

    const routingControl = (L as any).Routing.control({

      createMarker: () => null,

      waypoints: stops.map(
        (stop) =>
          L.latLng(stop.latitude, stop.longitude)
      ),

      routeWhileDragging: false,

      addWaypoints: false,

      draggableWaypoints: false,

      fitSelectedRoutes: true,

      show: false,
    }).addTo(map);

    return () => {
      map.removeControl(routingControl);
    };
  }, [stops, map]);

  return null;
}