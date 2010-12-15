%Yongzuan Wu wu68 cs450 HW6 10.1 (b)
format long;
stepsize=[1 3 7 15];
for k=1:4
    n=stepsize(k);
    y0=linspace(0,1,n+2)';
    y=fsolve(@finitdif,y0);
    x=linspace(0,1,n+2)';
    figure(k);
    plot(x,y);
    title(['n= ' num2str(n)])
end


    