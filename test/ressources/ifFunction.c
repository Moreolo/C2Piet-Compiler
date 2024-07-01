int x = 0;
int add(int a, int b){

	y = (a + b);

	print(a);

	return (a + b);
}

int main(){
	if (x<=0){
		x = add(x,5);
	}else{
		x = add(x, add(x,1));
	}	
 
}
