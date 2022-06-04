package vo;

import lombok.Getter;

@Getter
public class ModelNumber {
    private String productCode;
    private String branch;
    private String lot;

    public ModelNumber(String productCode, String branch, String lot) {
        if (productCode == null) throw new IllegalArgumentException("productCode 가 null 입니다.");
        if (branch == null) throw new IllegalArgumentException("branch 가 null 입니다.");
        if (lot == null) throw new IllegalArgumentException("lot 가 null 입니다.");

        this.productCode = productCode;
        this.branch = branch;
        this.lot = lot;
    }

    @Override
    public String toString() {
        return productCode + "-" + branch + "-" + lot;
    }
}
