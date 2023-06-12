import { Carpark } from "./carpark";

export interface Results {
    id : string,
    destination : string,
    distance : number,
    listOfParkedTime : string[],
    listOfExitTime : string[],
    dayOfWeek : string[],
    listOfCarParks : Carpark[]
}