%Yongzuan Wu wu68 cs450 HW6 10.1 (c)
format long;
stepsize=[3 4 5 6];
for k=1:4
    n=stepsize(k);
    x0=linspace(0,1,n)';
    x=fsolve(@colloc,x0)';
    for i=1:floor(n/2)
        t=x(1,i);
        x(1,i)=x(1,n-i+1);
        x(1,n-i+1)=t;
    end
    xx=linspace(0,1,n);
    y=polyval(x,xx);
    figure(k);
    plot(xx,y);
    title(['n= ' num2str(n)])
end


    