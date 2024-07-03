int x = 0;
int add(int a, int b){
	return (a + b);
}

int main(){
	if (x < add(3, 4)){
		x = add(x,5);
	}else{
		x = add(x, add(x,1));
	}

}
