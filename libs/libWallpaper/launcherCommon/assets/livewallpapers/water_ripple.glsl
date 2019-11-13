// Assume touch history and device density ratio has been passed in

const float AMPLITUDE = 1.8;
const float T_FREQUENCY = 2.5;
const float S_FREQUENCY = 0.08;
const float T_ATTENUATION = 3.8;
const float S_ATTENUATION = 0.03;
const float PROPAGATION = 200.0;

vec2 calculateOffset(vec2 relativePosition, float age)
{
    float distance = length(relativePosition);
    return AMPLITUDE * relativePosition

            // Apply temporal & spacial movement
            * sin(T_FREQUENCY * age + S_FREQUENCY * distance)

            // Attenuate with time & distance
            * exp(-(T_ATTENUATION * age + S_ATTENUATION * abs(distance - PROPAGATION * age)));
}

vec2 applyWaterRipple(vec2 fragCoord)
{
    vec2 offsetSum = vec2(0.0, 0.0);

    for (int n = 0; n < pointerHistorySize; ++n)
    {
        vec2 pointer = pointerHistory[n].xy;
        float pointerTime = pointerHistory[n].z;

        vec2 relativePosition = (fragCoord - pointer) / densityRatio;
        float age = time - pointerTime;

        offsetSum += calculateOffset(relativePosition, age);
    }
    return fragCoord + offsetSum;
}
