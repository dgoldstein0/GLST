function stop = outfun(inislop, optimValues, state)
stop = false;
hold on;
s=shooting(inislop);
drawnow