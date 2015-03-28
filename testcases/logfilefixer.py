#! python

import sys
import re

new_class_map = {
    "SimulateAction": "galactic_strategy.testing.SimulateAction",
    "CancelFacilityBuildOrder": "galactic_strategy.user_actions.CancelFacilityBuildOrder",
    "FacilityBuildOrder": "galactic_strategy.user_actions.FacilityBuildOrder",
    "ShipAttackMoveOrder": "galactic_strategy.user_actions.ShipAttackMoveOrder",
    "ShipAttackOrder": "galactic_strategy.user_actions.ShipAttackOrder",
    "ShipMoveOrder": "galactic_strategy.user_actions.ShipMoveOrder",
    "ShipInvadeOrder": "galactic_strategy.user_actions.ShipInvadeOrder",
    "ShipPickupTroopsOrder": "galactic_strategy.user_actions.ShipPickupTroopsOrder",
    "ShipWarpOrder": "galactic_strategy.user_actions.ShipWarpOrder",
    "ShipyardBuildShipOrder": "galactic_strategy.user_actions.ShipyardBuildShipOrder",
    "ShipyardCancelBuildOrder": "galactic_strategy.user_actions.ShipyardCancelBuildOrder",
    "FacilityType": "galactic_strategy.game_objects.FacilityType",
    "FacilityDescriber": "galactic_strategy.sync_engine.FacilityDescriber",
    "GSystemDescriber": "galactic_strategy.sync_engine.GSystemDescriber",
    "MissileDescriber": "galactic_strategy.sync_engine.MissileDescriber",
    "SatelliteDescriber": "galactic_strategy.sync_engine.SatelliteDescriber",
    "ShipDescriber": "galactic_strategy.sync_engine.ShipDescriber",
    "ShipType": "galactic_strategy.game_objects.ShipType",
    "DestinationPoint": "galactic_strategy.game_objects.DestinationPoint",
    "Order": "galactic_strategy.user_actions.Order",
}

def replace_class(match):
    return ''.join([
        'class="',
        new_class_map.get(match.group(1), match.group(1))
    ])

for filename in sys.argv[1:]:
    with open(filename, 'r') as f:
        lines = [l for l in f]

    with open(filename, 'w') as f:
        for line in lines:
            line = line.replace('SimulateAction', 'SimulateAction')
            line = re.sub('class="([^"\$]*)', replace_class, line)
            f.write(line)
